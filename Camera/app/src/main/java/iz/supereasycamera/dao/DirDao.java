package iz.supereasycamera.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import iz.supereasycamera.dto.MainDto;

/**
 * Created by tono on 2014/11/29.
 */
public final class DirDao {
    static final String CREATE = new StringBuilder()
            .append("CREATE TABLE DirData (")
            .append(" id INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(" createdAt INTEGER,")
            .append(" name TEXT,")
            .append(" parentId INTEGER NOT NULL DEFAULT 0")
            .append(")")
            .toString();

    private static final String SEL_BY_PK = "SELECT * FROM DirData WHERE id = ?";
    private static final String SEL_CHILDREN = "SELECT * FROM DirData WHERE parentId = ? ORDER BY name, id";

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
        return DaoHelper.getWritableDB(context).insert("DirData", null, values);
    }

    /**
     * DELETE
     *
     * @param context
     * @param id
     */
    public void delete(Context context, long id) {
        DaoHelper.getWritableDB(context).delete("DirData", "id = ?" ,new String[] {String.valueOf(id)});
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
     * SELECT Children
     *
     * @param context
     * @param id
     * @return list of dto
     */
    public List<MainDto> selectChildrenOf(Context context, long id) {
        List<MainDto> list = new ArrayList<MainDto>();
        Cursor c = DaoHelper.getReadableDB(context).rawQuery(SEL_CHILDREN, new String[]{String.valueOf(id)});
        while (c.moveToNext()) {
            list.add(mapRow(c));
        }
        return list;
    }

    private MainDto mapRow(Cursor c) {
        final MainDto dto = new MainDto();
        dto.dirOrPic = MainDto.DirOrPic.DIR;
        dto.id = c.getLong(0);
        dto.createdAt = new DateTime(c.getLong(1));
        dto.name = c.getString(2);
        dto.parentId = c.getLong(3);
        return dto;
    }

}
