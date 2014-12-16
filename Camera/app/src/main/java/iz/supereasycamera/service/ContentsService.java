package iz.supereasycamera.service;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import iz.supereasycamera.R;
import iz.supereasycamera.dao.DaoHelper;
import iz.supereasycamera.dao.PicDao;
import iz.supereasycamera.dto.StorageUsage;
import iz.supereasycamera.utils.Misc;
import iz.supereasycamera.dao.DirDao;
import iz.supereasycamera.dto.MainDto;

/**
 * Created by izumi_j on 2014/12/01.
 */
public final class ContentsService {
    private final DirDao dirDao = new DirDao();
    private final PicDao picDao = new PicDao();

    /**
     * ディレクトリを返す。
     *
     * @param context
     * @param id
     * @return dto
     */
    public MainDto getDir(Context context, long id) {
        final MainDto dto = dirDao.selectBy(context, id);
        Misc.debug(dto != null ? dto.toString() : "DirData not found by id = " + id);
        return dto;
    }

    /**
     * 指定ディレクトリID直下のデータ一覧を返す。
     *
     * @param context
     * @param dirId
     * @return list of dto
     */
    public List<MainDto> getDataListOf(Context context, long dirId) {
        List<MainDto> list = new ArrayList<MainDto>();

        list.addAll(dirDao.selectChildrenOf(context, dirId));
        list.addAll(picDao.selectChildrenOf(context, dirId));
        Misc.debug(list.size() + " dirs and pics found in " + dirId);

        return list;
    }

    /**
     * 指定ディレクトリID直下のディレクトリ一覧を返す。
     *
     * @param context
     * @param dirId
     * @return list of dto
     */
    public List<MainDto> getDirListOf(Context context, long dirId) {
        List<MainDto> list = new ArrayList<MainDto>();

        list.addAll(dirDao.selectChildrenOf(context, dirId));
        Misc.debug(list.size() + " dirs found in " + dirId);

        return list;
    }

    /**
     * 親を辿って「hoge/fuga/hige」といったディレクトリ構造を表す文字列を返す。
     *
     * @param context
     * @param dto
     * @return text
     */
    public String getFullyDirText(Context context, MainDto dto) {
        final String root = String.valueOf(context.getResources().getText(R.string.root_dir));

        if (dto == null) {
            return "";
        }
        if (dto.parentId == 0) {
            return root;
        }

        List<String> nameList = new ArrayList<String>();
        MainDto parent = getDir(context, dto.parentId);
        while (parent != null) {
            nameList.add(parent.name);
            parent = getDir(context, parent.parentId);
        }

        StringBuilder result = new StringBuilder(root);
        for (int i = nameList.size() - 1; i >= 0; i--) {
            result.append("/").append(nameList.get(i));
        }
        return result.toString();
    }

    /**
     * 新規ディレクトリを追加。
     *
     * @param context
     * @param name
     * @param parentId
     * @return dto
     */
    public MainDto addNewDir(Context context, String name, long parentId) {
        final MainDto result = new MainDto();
        result.dirOrPic = MainDto.DirOrPic.DIR;
        result.createdAt = DateTime.now();
        result.name = name;
        result.parentId = parentId;
        result.id = dirDao.insert(context, result);
        Misc.debug("New id for DirData is " + result.id);
        return result;
    }

    /**
     * 写真もしくはディレクトリを削除。
     *
     * @param context
     * @param dto
     */
    public void remove(Context context, MainDto dto) {
        switch (dto.dirOrPic) {
            case DIR:
                removeDir(context, dto.id);
                break;
            case PIC:
                picDao.delete(context, dto.id);
                break;
            default: throw  new IllegalArgumentException("Unknown DirOrPic!");
        }
    }

    /**
     * @param context
     * @param id
     */
    private void removeDir(Context context, long id) {
        // 子ディレクトリ削除
        List<MainDto> dirChildren = dirDao.selectChildrenOf(context, id);
        for (MainDto dirChild : dirChildren) {
            removeDir(context, dirChild.id);
        }

        // 子ファイル削除
        picDao.deleteChildrenOf(context, id);

        // 自分を削除
        dirDao.delete(context, id);
    }

    /**
     * 新しい写真を追加。
     *
     * @param context
     * @param parentId
     * @param orientation
     * @param content
     * @return dto
     */
    public MainDto addNewPic(Context context, long parentId, int orientation, byte[] content) {
        final MainDto result = new MainDto();
        result.dirOrPic = MainDto.DirOrPic.PIC;
        result.createdAt = DateTime.now();
        result.name = Misc.formatDateTime(result.createdAt);
        result.parentId = parentId;
        result.size = content.length;
        result.orientation = orientation;
        result.content = content;
        result.id = picDao.insert(context, result);

        result.content = null;// DTOに写真データは保持しないよ
        Misc.debug("New PicData inserted. " + result.toString());
        return result;
    }

    /**
     * 写真データを取得。
     *
     * @param context
     * @param id
     * @return bytes
     */
    public byte[] getPicture(Context context, long id) {
        return picDao.selectContentBy(context, id);
    }

    /**
     *
     * @param context
     * @param dto
     */
    public void updateName(Context context, MainDto dto) {
        switch (dto.dirOrPic) {
            case DIR:
                dirDao.updateName(context, dto.id, dto.name);
                break;
            case PIC:
                picDao.updateName(context, dto.id, dto.name);
                break;
            default: throw new IllegalArgumentException("Unknown DirOrPic!");
        }
    }

    /**
     *
     * @param context
     * @return usage
     */
    public StorageUsage getStorageUsage(Context context) {
        final int numDirs = dirDao.selectCount(context);
        final int numPics = picDao.selectCount(context);
        final int ttlSize = picDao.selectTotalSize(context);
        return new StorageUsage(numDirs, numPics, ttlSize);
    }

    /**
     *
     * @param dtos
     * @param parentId
     * @return true if success
     */
    public boolean changeParent(Context context, List<MainDto> dtos, long parentId) {
        DaoHelper.beginTransaction(context);
        for (MainDto dto : dtos) {
            switch (dto.dirOrPic) {
                case DIR:
                    if (isDescendant(context, parentId,  dto.id)) {
                        Misc.warn("Cannot move directory into itself. id = " + dto.id);
                        return false;
                    }
                    dirDao.updateParentId(context, dto.id, parentId);
                    break;
                case PIC:
                    picDao.updateParentId(context, dto.id, parentId);
                    break;
                default: throw new IllegalArgumentException("Unknown DirOrPic!");
            }
        }
        DaoHelper.commitTransaction(context);
        return true;
    }

    /**
     * @param context
     * @param targetId
     * @param selfId
     * @return true if target is descendant of self
     */
    public boolean isDescendant(Context context, long targetId, long selfId) {
        long wkId = targetId;
        while (wkId > 0) {
            if (wkId == selfId) {
                return true;
            }

            final MainDto target = dirDao.selectBy(context, wkId);
            final MainDto nextParent = dirDao.selectBy(context, target.parentId);
            wkId = nextParent != null ? nextParent.id : 0;
        }

        return false;
    }
}
