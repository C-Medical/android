package iz.supereasycamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import iz.supereasycamera.service.ContentsService;
import iz.supereasycamera.utils.Misc;
import iz.supereasycamera.utils.PictureUtils;


public class PictureActivity extends Activity {

    private long picId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_picture);

        Intent intent = getIntent();
        if(intent != null) {
            picId = intent.getLongExtra("id", 0);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.some_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        final ImageView imgPic = (ImageView) findViewById(R.id.imgPic);
        final FrameLayout layout = (FrameLayout) findViewById(R.id.layoutPicMain);
        new BitmapWorkerTask(imgPic, picId, layout.getWidth(), layout.getHeight()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 非同期に画像をdecodeするタスク
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final ContentsService contentsService = new ContentsService();
        private final ImageView imageView;
        private final long id;
        private final long width;
        private final long height;

        private BitmapWorkerTask(ImageView imageView, long id, long width, long height) {
            this.imageView = imageView;
            this.id = id;
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Misc.debug("Start to create Bitmap of " + id);
            final byte[] picture = contentsService.getPicture(getApplicationContext(), id);
            final Bitmap ret = PictureUtils.toBitmap(picture, width, height);
            if (ret == null) {
                Misc.warn("Picture is null! id = " + id);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Misc.debug("Finish task for " + id);

            if (isCancelled()) {
                Misc.debug("Task has been canceled.");
                bitmap = null;
                return;
            }

            if (bitmap == null) {
                Misc.debug("bitmap is null for " + id);
                return;
            }

            Misc.debug("Set Bitmap of " + id);
            imageView.setImageBitmap(bitmap);
        }
    }
}
