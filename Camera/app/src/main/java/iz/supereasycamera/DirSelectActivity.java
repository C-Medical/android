package iz.supereasycamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import iz.supereasycamera.dto.MainDto;
import iz.supereasycamera.service.ContentsService;


public class DirSelectActivity extends Activity {
    private final ContentsService contentsService = new ContentsService();
    private MainDto currentDir = null;
    private MainListAdapter listAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dir_select);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new ItemClickListener());
        listAdapter = new MainListAdapter(this, 0);
        listView.setAdapter(listAdapter);

        ((ImageButton) findViewById(R.id.btnLeft)).setOnClickListener(new PrevButtonClickListener());
        ((Button) findViewById(R.id.btnOk)).setOnClickListener(new OkButtonClickListener());

        load();
    }

    private long getCurrentDirId() {
        return currentDir == null ? 0 : currentDir.id;
    }

    private void load() {
        listAdapter.clear();
        listAdapter.addAll(contentsService.getDirListOf(getApplicationContext(), getCurrentDirId()));

        final TextView txtDir = (TextView) findViewById(R.id.txtDir);
        txtDir.setText(currentDir != null ? "/" + currentDir.name : getResources().getText(R.string.root_dir));

        final TextView txtDirTree = (TextView) findViewById(R.id.txtDirTree);
        txtDirTree.setText(contentsService.getFullyDirText(getApplicationContext(), currentDir));
    }

    /**
     * 戻るボタンイベント
     */
    private class PrevButtonClickListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentDir == null) {
                return;
            }

            if (currentDir.parentId == 0) {
                currentDir = null;
                load();
                return;
            }

            currentDir = contentsService.getDir(getApplicationContext(), currentDir.parentId);
            load();
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
                    DirSelectActivity.this.currentDir = dto;
                    DirSelectActivity.this.load();
                    break;
                default: throw new IllegalStateException("Should not reach here!");
            }
        }
    }

    /**
     * OKボタンイベント
     */
    private class OkButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final Intent intent = getIntent();
            final ArrayList<MainDto> dtos = intent.getParcelableArrayListExtra("dtos");
            if (contentsService.changeParent(getApplicationContext(), dtos, getCurrentDirId())) {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.data_moved), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.data_not_moved), Toast.LENGTH_LONG).show();
            }
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
