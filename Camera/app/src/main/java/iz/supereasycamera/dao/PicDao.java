package iz.supereasycamera.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import iz.supereasycamera.dto.MainDto;

/**
 * Created by izumi_j on 2014/11/29.
 */
public final class PicDao {
    static final String CREATE = new StringBuilder()
            .append("CREATE TABLE PicData (")
            .append(" id INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(" createdAt INTEGER,")
            .append(" name TEXT,")
            .append(" parentId INTEGER NOT NULL DEFAULT 0,")
            .append(" content BLOB,")
            .append(" size INTEGER NOT NULL DEFAULT 0")
            .append(")")
            .toString();

    private static final String SEL_BY_PK = "SELECT * FROM PicData WHERE id = ?";
    private static final String SEL_CHILDREN = "SELECT * FROM PicData WHERE parentId = ? ORDER BY name, id";

    /**
     * INSERT
     *
     * @param context
     * @param dto
     * @return new id
     */
    public long insert(Context context, MainDto dto) {
        final ContentValues values = new ContentValues();
        values.put("createdAt", dto.createdAt.getMillis());
        values.put("name", dto.name);
        values.put("parentId", dto.parentId);
        values.put("content", dto.content);
        values.put("size", dto.content.length);
        return DaoHelper.getWritableDB(context).insert("PicData", null, values);
    }

    /**
     * DELETE
     *
     * @param context
     * @param id
     */
    public void delete(Context context, long id) {
        DaoHelper.getWritableDB(context).delete("PicData", "id = ?" ,new String[] {String.valueOf(id)});
    }

    /**
     * DELETE Children
     *
     * @param context
     * @param parentId
     */
    public void deleteChildrenOf(Context context, long parentId) {
        DaoHelper.getWritableDB(context).delete("PicData", "parentId = ?" ,new String[] {String.valueOf(parentId)});
    }

    /**
     * SELECT
     *
     * @param context
     * @param id
     * @return dto
     */
    public MainDto selectBy(Context context, long id) {
        Cursor c = DaoHelper.getReadableDB(context).rawQuery(SEL_BY_PK, new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            return mapRow(c);
        } else {
            return null;
        }
    }

    /**
     * SELECT content
     *
     * @param context
     * @param id
     * @return bytes
     */
    public byte[] selectContentBy(Context context, long id) {
        Cursor c = DaoHelper.getReadableDB(context).rawQuery(SEL_BY_PK, new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            return c.getBlob(4);
        } else {
            return null;
        }
    }

    /**
     * SELECT Children
     *
     * @param context
     * @param parentId
     * @return list of dto
     */
    public List<MainDto> selectChildrenOf(Context context, long parentId) {
        List<MainDto> list = new ArrayList<MainDto>();
        Cursor c = DaoHelper.getReadableDB(context).rawQuery(SEL_CHILDREN, new String[]{String.valueOf(parentId)});
        while (c.moveToNext()) {
            list.add(mapRow(c));
        }
        return list;
    }

    private MainDto mapRow(Cursor c) {
        final MainDto dto = new MainDto();
        dto.dirOrPic = MainDto.DirOrPic.PIC;
        dto.id = c.getLong(0);
        dto.createdAt = new DateTime(c.getLong(1));
        dto.name = c.getString(2);
        dto.parentId = c.getLong(3);
        dto.content = null;
        dto.size = c.getInt(5);
        return dto;
    }

    /**
     * @param context
     * @param id
     * @param name
     */
    public void updateName(Context context, long id, String name) {
        final ContentValues values = new ContentValues();
        values.put("name", name);
        DaoHelper.getWritableDB(context).update("PicData", values, "id = ?", new String[]{String.valueOf(id)});
    }

}
