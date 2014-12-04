package iz.supereasycamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import iz.supereasycamera.dto.MainDto;
import iz.supereasycamera.service.ContentsService;
import iz.supereasycamera.utils.PictureUtils;


public class MainActivity extends Activity {
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_REF_PIC = 3;
    private static final int REQUEST_CODE_MOVE_DIR = 4;

    private final ContentsService contentsService = new ContentsService();
    private MainDto currentDir = null;
    private MainListAdapter listAdapter;
    private ListView listView;
    private Uri tempImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Buttonクリックイベントを設定
        ImageButton btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new PrevButtonClickListener());
        ImageButton btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new AddButtonClickListener());
        ImageButton btnDel = (ImageButton) findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new DelButtonClickListener());
        ImageButton btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new CameraButtonClickListener());
        ImageButton btnMoveDir = (ImageButton) findViewById(R.id.btnMoveDir);
        btnMoveDir.setOnClickListener(new MoveDirButtonClickListener());

        // ListViewのイベントや中身を管理するクラスを設定
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new ItemClickListener());
        listView.setOnItemLongClickListener(new ItemLongClickListener());
        listAdapter = new MainListAdapter(this, 0);
        listView.setAdapter(listAdapter);

        // 初期表示データを取得して追加
        load();
    }

    private long getCurrentDirId() {
        return currentDir == null ? 0 : currentDir.id;
    }

    private void load() {
        listAdapter.clear();
        listAdapter.addAll(contentsService.getDataListOf(getApplicationContext(), getCurrentDirId()));

        final TextView txtDir = (TextView) findViewById(R.id.txtDir);
        txtDir.setText(currentDir != null ? "/" + currentDir.name : getResources().getText(R.string.root_dir));

        final TextView txtDirTree = (TextView) findViewById(R.id.txtDirTree);
        txtDirTree.setText(contentsService.getFullyDirText(getApplicationContext(), currentDir));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // オプションメニューのレイアウトを設定
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // オプションメニューが選択されたときの処理
        int id = item.getItemId();

        if (id == R.id.action_usage) {
            final Intent intent = new Intent(getApplicationContext(), StorageUsageActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("currentDirId", getCurrentDirId());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final long currentDirId = savedInstanceState.getLong("currentDirId", 0);
        if (currentDirId > 0) {
            currentDir = contentsService.getDir(getApplicationContext(), currentDirId);
        }
        load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (resultCode != RESULT_OK) {
                    return;
                }
                // 取った写真をbyte配列で取得
                byte[] picture = PictureUtils.readOutFrom(getApplicationContext(), tempImageUri);
                getContentResolver().delete(tempImageUri, null, null);
                if (picture.length == 0 && data.getData() != null) {
                    // Xperia対応らしい！
                    picture = PictureUtils.readOutFrom(getApplicationContext(), data.getData());
                    getContentResolver().delete(data.getData(), null, null);
                }
                // DB保存して、一覧に反映
                final MainDto dto = contentsService.addNewPic(getApplicationContext(), getCurrentDirId(), picture);
                listAdapter.add(dto);
                break;

            case REQUEST_CODE_MOVE_DIR:
                if (resultCode != RESULT_OK) {
                    return;
                }
                load();
                break;
        }
    }

    /**
     * 戻るボタンイベント
     */
    private class PrevButtonClickListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (MainActivity.this.currentDir == null) {
                return;
            }

            if (MainActivity.this.currentDir.parentId == 0) {
                MainActivity.this.currentDir = null;
                MainActivity.this.load();
                return;
            }

            MainActivity.this.currentDir = MainActivity.this.contentsService.getDir(getApplicationContext(), MainActivity.this.currentDir.parentId);
            MainActivity.this.load();
        }
    }

    /**
     * ディレクトリ追加ボタンイベント
     */
    private class AddButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText editText = new EditText(MainActivity.this);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);

            final AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(v.getResources().getString(R.string.enter_name))
                    .setView(editText)
                    .setPositiveButton(v.getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            final String entry = editText.getText().toString().trim();
                            if (entry.length() == 0) {
                                return;
                            }

                            final MainDto dto = MainActivity.this.contentsService.addNewDir(getApplicationContext(), entry, getCurrentDirId());
                            MainActivity.this.listAdapter.add(dto);
                        }
                    })
                    .setNegativeButton(v.getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .create();

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });

            dlg.show();
        }
    }

    /**
     * 項目削除ボタンイベント
     */
    private class DelButtonClickListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            final List<MainDto> dtos = MainActivity.this.listAdapter.getSelectedDtos();
            if (dtos.isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.not_selected), Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(v.getResources().getString(R.string.confirm))
                    .setMessage(v.getResources().getString(R.string.confirm_del))
                    .setPositiveButton(v.getResources().getText(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            for (MainDto dto : dtos) {
                                MainActivity.this.contentsService.remove(getApplicationContext(), dto);
                                MainActivity.this.listAdapter.remove(dto);
                            }
                        }
                    })
                    .setNegativeButton(v.getResources().getText(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
        }
    }

    /**
     * 項目選択イベント
     */
    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final MainDto dto = (MainDto)((ListView) parent).getItemAtPosition(position);
            switch (dto.dirOrPic) {
                case DIR:
                    MainActivity.this.currentDir = dto;
                    MainActivity.this.load();
                    break;
                case PIC:
                    Intent intent = new Intent(getApplicationContext(), PictureActivity.class);
                    intent.putExtra("id", dto.id);
                    startActivityForResult(intent, REQUEST_CODE_REF_PIC);
                    break;
                default: throw new IllegalArgumentException("Unknown DirOrPic!");
            }
        }
    }

    /**
     * 項目長押しイベント
     */
    private class ItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final MainDto dto = MainActivity.this.listAdapter.getItem(position);

            final EditText editText = new EditText(MainActivity.this);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setText(dto.name);
            editText.selectAll();

            final AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(view.getResources().getString(R.string.enter_name))
                    .setView(editText)
                    .setPositiveButton(view.getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            final String entry = editText.getText().toString().trim();
                            if (entry.length() == 0) {
                                return;
                            }

                            dto.name = entry;
                            MainActivity.this.contentsService.updateName(getApplicationContext(), dto);
                            MainActivity.this.listAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(view.getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .create();

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });

            dlg.show();
            return false;
        }
    }

    /**
     * フォルダ移動ボタンイベント
     */
    private class MoveDirButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final ArrayList<MainDto> dtos = MainActivity.this.listAdapter.getSelectedDtos();
            if (dtos.isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.not_selected), Toast.LENGTH_SHORT).show();
                return;
            }

            final Intent intent = new Intent(getApplicationContext(), DirSelectActivity.class);
            intent.putParcelableArrayListExtra("dtos", dtos);
            startActivityForResult(intent, REQUEST_CODE_MOVE_DIR);
        }
    }

    /**
     * カメラボタンイベント
     */
    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final String fileName = "SuperEasyCamera" + System.currentTimeMillis() + ".jpg";

            final ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            MainActivity.this.tempImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, MainActivity.this.tempImageUri);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }
    }
}
