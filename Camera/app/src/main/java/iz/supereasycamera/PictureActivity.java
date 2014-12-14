package iz.supereasycamera;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import iz.supereasycamera.dto.MainDto;
import iz.supereasycamera.service.ContentsService;
import iz.supereasycamera.utils.Misc;
import iz.supereasycamera.utils.PictureUtils;


public class PictureActivity extends Activity implements ViewSwitcher.ViewFactory {
    private static final int REQ_CODE_SHARE = 2;

    private MainDto pic;
    private ArrayList<MainDto> pics;
    private Uri tempImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_picture);

        Intent intent = getIntent();
        if(intent != null) {
            pic = intent.getParcelableExtra("pic");
            pics = intent.getParcelableArrayListExtra("pics");
        } else {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.some_error), Toast.LENGTH_LONG).show();
        }

        ((TextView)findViewById(R.id.txtPicName)).setText(pic.name);
        ((ImageSwitcher)findViewById(R.id.imageSwitcher)).setFactory(this);

        ((ImageButton)findViewById(R.id.btnShare)).setOnClickListener(new ShareButtonClickListener());
        ((ImageButton)findViewById(R.id.btnLeft)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pics.indexOf(pic);
                if (index == 0) {
                    return;
                }
                pic = pics.get(index - 1);
                ((TextView)findViewById(R.id.txtPicName)).setText(pic.name);
                showImage();
            }
        });
        ((ImageButton)findViewById(R.id.btnRight)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pics.indexOf(pic);
                if (index >= pics.size() - 1) {
                    return;
                }
                pic = pics.get(index + 1);
                ((TextView)findViewById(R.id.txtPicName)).setText(pic.name);
                showImage();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        showImage();
    }

    private void showImage() {
        final ImageSwitcher imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        final FrameLayout layout = (FrameLayout) findViewById(R.id.layoutPicMain);
        new BitmapWorkerTask(imageSwitcher, pic, layout.getWidth(), layout.getHeight()).execute();
    }

    @Override
    public View makeView() {
        final ImageView v = new ImageView(getApplicationContext());
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return v;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (tempImageUri != null) {
            getContentResolver().delete(tempImageUri, null, null);
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("pic", pic);
        outState.putParcelableArrayList("pics", pics);
        outState.putParcelable("tempImageUri", tempImageUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pic = savedInstanceState.getParcelable("pic");
        pics = savedInstanceState.getParcelableArrayList("pics");
        tempImageUri = savedInstanceState.getParcelable("tempImageUri");
    }

    /**
     * シェアボタンイベント
     */
    private class ShareButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tempImageUri = null;

            // 画像を仮保存
            final ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            tempImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream os = null;
            try {
                os = getContentResolver().openOutputStream(tempImageUri);
                final ImageSwitcher imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
                final ImageView imageView = (ImageView) imageSwitcher.getCurrentView();
                final Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            } catch (IOException e){
                Misc.error("Failed to save temporary image.");
                return;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e){}
                }
            }

            // Share
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, tempImageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.share_picture)), REQ_CODE_SHARE);
            } catch (ActivityNotFoundException ex){

            }
        }
    }

    /**
     * 非同期に画像をdecodeするタスク
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final ContentsService contentsService = new ContentsService();
        private final ImageSwitcher imageView;
        private final MainDto pic;
        private final long width;
        private final long height;

        private BitmapWorkerTask(ImageSwitcher imageView, MainDto pic, long width, long height) {
            this.imageView = imageView;
            this.pic = pic;
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Misc.debug("Start to create Bitmap of " + pic.id);
            final byte[] picture = contentsService.getPicture(getApplicationContext(), pic.id);
            final Bitmap ret = PictureUtils.toBitmap(picture, width, height, pic.orientation);
            if (ret == null) {
                Misc.warn("Picture is null! id = " + pic.id);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Misc.debug("Finish task for " + pic.id);

            if (isCancelled()) {
                Misc.debug("Task has been canceled.");
                bitmap = null;
                return;
            }

            if (bitmap == null) {
                Misc.debug("bitmap is null for " + pic.id);
                return;
            }

            Misc.debug("Set Bitmap of " + pic.id);
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageDrawable(new BitmapDrawable(getResources(),bitmap));
        }
    }
}
