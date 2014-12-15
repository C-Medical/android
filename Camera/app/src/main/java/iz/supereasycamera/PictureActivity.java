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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import iz.supereasycamera.dto.MainDto;
import iz.supereasycamera.service.ContentsService;
import iz.supereasycamera.utils.Misc;
import iz.supereasycamera.utils.PictureUtils;


public class PictureActivity extends Activity implements GestureDetector.OnGestureListener {
    private static final int REQ_CODE_SHARE = 2;

    private MainDto pic;
    private ArrayList<MainDto> pics;
    private Uri tempImageUri;

    private FrameLayout layout;
    private GestureDetector gestureDetector;
    private ViewFlipper flipper;
    private Animation slideInFromLeft;
    private Animation slideInFromRight;
    private Animation slideOutToLeft;
    private Animation slideOutToRight;

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

        ((ImageButton) findViewById(R.id.btnShare)).setOnClickListener(new ShareButtonClickListener());

        ((TextView)findViewById(R.id.txtPicName)).setText(pic.name);

        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        flipper.addView(createImageView());
        flipper.addView(createImageView());
        flipper.addView(createImageView());
        flipper.showNext();

        slideInFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left);
        slideInFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right);
        slideOutToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_left);
        slideOutToRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_right);
        gestureDetector = new GestureDetector(this, this);
    }

    private ImageView createImageView() {
        final ImageView v = new ImageView(getApplicationContext());
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return v;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        layout = (FrameLayout) findViewById(R.id.layoutPicMain);

        final int index = pics.indexOf(pic);
        final MainDto prevPic = index > 0 ? pics.get(index - 1) : null;
        final MainDto nextPic = index < pics.size() - 1 ? pics.get(index + 1) : null;

        new BitmapWorkerTask((ImageView) flipper.getChildAt(0), prevPic, layout.getWidth(), layout.getHeight()).execute();
        new BitmapWorkerTask((ImageView) flipper.getChildAt(1), pic, layout.getWidth(), layout.getHeight()).execute();
        new BitmapWorkerTask((ImageView) flipper.getChildAt(2), nextPic, layout.getWidth(), layout.getHeight()).execute();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent e) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    @Override
    public  boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }
    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.dispatchTouchEvent(event);
    }

    @Override
    public final boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        float dx = Math.abs(e1.getX() - e2.getX());
        float dy = Math.abs(e1.getY() - e2.getY());
        if (dx < dy) {
            return false;
        }

        int index = pics.indexOf(pic);
        if (velocityX > 0) {
            if (index <= 0) {
                Toast.makeText(getApplicationContext(), "No more", Toast.LENGTH_SHORT).show();
                return false;
            }

            --index;
            this.pic = pics.get(index);
            ((TextView)findViewById(R.id.txtPicName)).setText(pic.name);
            final MainDto prevPic = index > 0 ? pics.get(index - 1) : null;

            flipper.setInAnimation(slideInFromLeft);
            flipper.setOutAnimation(slideOutToRight);
            flipper.showPrevious();

            // 3つ目を先頭へ移す
            final ImageView last = (ImageView) flipper.getChildAt(2);
            flipper.removeViewAt(2);
            flipper.addView(last, 0);

            // 画像交換
            new BitmapWorkerTask(last, prevPic, layout.getWidth(), layout.getHeight()).execute();
        } else {
            // 次へ
            if (index >= pics.size() - 1) {
                Toast.makeText(getApplicationContext(), "No more", Toast.LENGTH_SHORT).show();
                return false;
            }

            ++index;
            this.pic = pics.get(index);
            ((TextView)findViewById(R.id.txtPicName)).setText(pic.name);
            final MainDto nextPic = index < pics.size() - 1 ? pics.get(index + 1) : null;

            flipper.setInAnimation(slideInFromRight);
            flipper.setOutAnimation(slideOutToLeft);
            flipper.showNext();

            // 1つ目を最後へ移す
            final ImageView first = (ImageView) flipper.getChildAt(0);
            flipper.removeViewAt(0);
            flipper.addView(first, 2);

            // 画像交換
            new BitmapWorkerTask(first, nextPic, layout.getWidth(), layout.getHeight()).execute();
        }
        return true;
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
                final ImageView imageView = (ImageView) flipper.getCurrentView();
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
        private final ImageView imageView;
        private final MainDto pic;
        private final long width;
        private final long height;

        private BitmapWorkerTask(ImageView imageView, MainDto pic, long width, long height) {
            this.imageView = imageView;
            this.pic = pic;
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (pic == null) {
                Misc.debug("No pic");
                return null;
            }

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
            if (isCancelled()) {
                Misc.debug("Task has been canceled.");
                bitmap = null;
                return;
            }

            if (bitmap == null) {
                imageView.setImageBitmap(null);
                return;
            }

            Misc.debug("Set Bitmap of " + pic.id);
            imageView.setImageBitmap(bitmap);
        }
    }
}
