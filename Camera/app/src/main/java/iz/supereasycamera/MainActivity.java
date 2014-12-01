package iz.supereasycamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import iz.supereasycamera.dto.MainDto;
import iz.supereasycamera.service.ContentsService;


public class MainActivity extends Activity {
    private static final int REQUEST_CODE_CAMERA = 2;

    private final ContentsService contentsService = new ContentsService();
    private MainDto currentDir = null;
    private MainListAdapter listAdapter;
    private ListView listView;
    private Uri tempImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // ListViewのイベントや中身を管理するクラスを設定
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new ItemClickListener());
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
        listAdapter.addAll(contentsService.getContentsOf(getApplicationContext(), getCurrentDirId()));
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

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (resultCode != RESULT_OK) {
                    return;
                }
                break;
        }
    }

    /**
     * 戻るボタンイベント
     */
    private class PrevButtonClickListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            final TextView txt = (TextView) MainActivity.this.findViewById(R.id.txtDir);

            if (MainActivity.this.currentDir == null) {
                return;
            }

            if (MainActivity.this.currentDir.parentId == 0) {
                MainActivity.this.currentDir = null;
                MainActivity.this.load();
                txt.setText(getResources().getText(R.string.root_dir));
                return;
            }

            MainActivity.this.currentDir = MainActivity.this.contentsService.getDir(getApplicationContext(), MainActivity.this.currentDir.parentId);
            MainActivity.this.load();
            txt.setText(getResources().getText(R.string.root_dir));
        }
    }

    /**
     * ディレクトリ追加ボタンイベント
     */
    private class AddButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText editView = new EditText(MainActivity.this);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(v.getResources().getString(R.string.enter_name))
                    .setView(editView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            final String entry = editView.getText().toString().trim();
                            if (entry.length() == 0) {
                                return;
                            }

                            final MainDto dto = MainActivity.this.contentsService.addNewDir(getApplicationContext(), entry, getCurrentDirId());
                            MainActivity.this.listAdapter.add(dto);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
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
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            for (MainDto dto : dtos) {
                                MainActivity.this.contentsService.remove(getApplicationContext(), dto);
                                MainActivity.this.listAdapter.remove(dto);
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                    final TextView txt = (TextView) MainActivity.this.findViewById(R.id.txtDir);
                    txt.setText(dto.name);
                    MainActivity.this.currentDir = dto;
                    MainActivity.this.load();
                    break;
                case PIC:
                    break;
                default: throw new IllegalArgumentException("Unknown DirOrPic!");
            }
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
