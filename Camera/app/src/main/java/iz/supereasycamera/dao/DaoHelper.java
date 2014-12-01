package iz.supereasycamera.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tono on 2014/11/29.
 */
public final class DaoHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SuperEasyCamera.db";
    private static final int DB_VER = 1;

    private static DaoHelper INSTANCE;

    synchronized public static DaoHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DaoHelper(context);
        }
        return INSTANCE;
    }

    public static SQLiteDatabase getWritableDB(Context context) {
        return getInstance(context).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDB(Context context) {
        return getInstance(context).getReadableDatabase();
    }

    private DaoHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DirDao.CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onCreate(db);
    }
}
