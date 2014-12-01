package iz.supereasycamera.service;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import iz.supereasycamera.Utils;
import iz.supereasycamera.dao.DirDao;
import iz.supereasycamera.dto.MainDto;

/**
 * Created by izumi_j on 2014/12/01.
 */
public final class ContentsService {
    private final DirDao dirDao = new DirDao();

    /**
     * ディレクトリを返す。
     *
     * @param context
     * @param id
     * @return dto
     */
    public MainDto getDir(Context context, long id) {
        final MainDto dto = dirDao.selectBy(context, id);
        Utils.debug(dto != null ? dto.toString() : "DirData not found by id = " + id);
        return dto;
    }

    /**
     * 指定ディレクトリID直下のコンテンツを返す。
     *
     * @param context
     * @param dirId
     * @return list of dto
     */
    public List<MainDto> getContentsOf(Context context, long dirId) {
        List<MainDto> list = new ArrayList<MainDto>();

        list.addAll(dirDao.selectChildrenOf(context, dirId));
        Utils.debug(list.size() + " dirs found in " + dirId);

        return list;
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
        Utils.debug("New id for DirData is " + result.id);
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

        // 自分を削除
        dirDao.delete(context, id);
    }
}
