package iz.supereasycamera.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by izumi_j on 2014/12/02.
 */
public final class PictureUtils {
    private PictureUtils(){}

    /**
     * 指定URIの画像のExifから向きを取得する。
     *
     * @param  context
     * @param uri
     * @return
     */
    public static int getOrientation(Context context, Uri uri) {
        if (uri == null) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
        try {
            final Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            final String path = cursor.getString(0);
            cursor.close();
            try {
                ExifInterface exif = new ExifInterface(path);
                return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException e) {
                Misc.error("Failed to get exif!", e);
                return ExifInterface.ORIENTATION_NORMAL;
            }
        } catch (Throwable e) {
            Misc.error("#getOrientation", e);
            throw new IllegalStateException(e);
        }
    }

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
        } catch (Throwable e) {
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
     * @param origin
     * @param orientation
     * @return bitmap
     */
    public static Bitmap rotate(Bitmap origin, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90f);
                break;
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return origin;
        }

        return Bitmap.createBitmap(origin, 0, 0, origin.getWidth(), origin.getHeight(), matrix, true);
    }


    /**
     * 一覧用のBitmapに変換する。
     *
     * @param picture
     * @param orientation
     * @return bitmap
     */
    public static Bitmap toIndexBitmap(byte[] picture, int orientation) {
        return toBitmap(picture, 32, 32, orientation);
    }

    /**
     * 指定したサイズのBitmapに変換する。
     *
     * @param picture
     * @return bitmap
     */
    public static Bitmap toBitmap(byte[] picture, long width, long height, int orientation) {
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
        Bitmap origin = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);

        return rotate(origin, orientation);
    }
}
