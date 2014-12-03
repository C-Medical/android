package iz.supereasycamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by izumi_j on 2014/12/02.
 */
public final class PictureUtils {
    private PictureUtils(){}

    /**
     * 指定URIから画像を読み取って、指定URIからは削除する。
     *
     * @param context
     * @param uri
     * @return
     */
    public static byte[] readOutFrom(Context context, Uri uri) {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            baos = new ByteArrayOutputStream();
            byte [] buffer = new byte[is.available()];
            while(true) {
                int len = is.read(buffer);
                if(len < 0) {
                    break;
                }
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            Misc.error("Failed to read data!", e);
            throw new IllegalStateException(e);
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * 一覧用のBitmapに変換する。
     *
     * @param picture
     * @return bitmap
     */
    public static Bitmap toIndexBitmap(byte[] picture) {
        return toBitmap(picture, 32, 32);
    }

    /**
     * 指定したサイズのBitmapに変換する。
     *
     * @param picture
     * @return bitmap
     */
    public static Bitmap toBitmap(byte[] picture, long width, long height) {
        if (picture == null || picture.length == 0) {
            return null;
        }

        // 画像サイズを先に取得
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(picture, 0, picture.length, options);

        // 縮尺を計算する
        Misc.debug("Original size = " + options.outWidth + "x" + options.outHeight);
        int scale = 1;
        if (options.outWidth > width || options.outHeight > height) {
            if (options.outWidth > options.outHeight) {
                scale = Math.round((float)options.outHeight / (float)height);
            } else {
                scale = Math.round((float)options.outWidth / (float)width);
            }
        }
        Misc.debug("Scale = " + scale);

        // Bitmapに読み込む
        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
    }
}
