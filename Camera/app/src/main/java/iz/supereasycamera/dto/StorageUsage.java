package iz.supereasycamera.dto;

/**
 * Created by izumi_j on 2014/12/04.
 */
public final class StorageUsage {
    public final int numberOfDirs;
    public final int numberOfPics;
    public final int totalBytes;

    public StorageUsage(int numberOfDirs, int numberOfPics, int totalBytes) {
        this.numberOfDirs = numberOfDirs;
        this.numberOfPics = numberOfPics;
        this.totalBytes = totalBytes;
    }
}
