package iz.supereasycamera.dto;

import org.joda.time.DateTime;

import java.util.Arrays;

/**
 * Created by tono on 2014/11/28.
 */
public final class MainDto {

    public enum DirOrPic {
        DIR, PIC;
    }

    public DirOrPic dirOrPic;
    public long id;
    public DateTime createdAt;
    public String name;
    public long parentId;
    public byte[] content;
    public int size;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MainDto mainDto = (MainDto) o;

        if (id != mainDto.id) return false;
        if (parentId != mainDto.parentId) return false;
        if (size != mainDto.size) return false;
        if (!Arrays.equals(content, mainDto.content)) return false;
        if (createdAt != null ? !createdAt.equals(mainDto.createdAt) : mainDto.createdAt != null) return false;
        if (dirOrPic != mainDto.dirOrPic) return false;
        if (name != null ? !name.equals(mainDto.name) : mainDto.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dirOrPic != null ? dirOrPic.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (parentId ^ (parentId >>> 32));
        result = 31 * result + (content != null ? Arrays.hashCode(content) : 0);
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "MainDto{" +
                "dirOrPic=" + dirOrPic +
                ", id=" + id +
                ", createdAt=" + createdAt +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", content=" + Arrays.toString(content) +
                ", size=" + size +
                '}';
    }

}
