package iz.supereasycamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.List;

import iz.supereasycamera.dao.DirDao;
import iz.supereasycamera.dto.MainDto;


public class MainActivity extends Activity {

    private MainDto currentDir = null;

    private MainListAdapter listAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listAdapter = new MainListAdapter(this, 0);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);

        ImageButton btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new AddButtonClickListener());
        ImageButton btnDel = (ImageButton) findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new DelButtonClickListener());

        listAdapter.addAll(new DirDao().selectChildrenOf(getApplicationContext(), 0));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

                            final MainDto dto = new MainDto();
                            dto.dirOrPic = MainDto.DirOrPic.DIR;
                            dto.createdAt = DateTime.now();
                            dto.name = entry;
                            if (MainActivity.this.currentDir != null) {
                                dto.parentId = MainActivity.this.currentDir.id;
                            }

                            final DirDao dao = new DirDao();
                            dto.id = dao.insert(getApplicationContext(), dto);
                            Log.d("DEBUG", "new id = " + dto.id);

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
                            // TODO 再帰的に消す
                            for (MainDto dto : dtos) {
                                new DirDao().delete(getApplicationContext(), dto.id);
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
}
