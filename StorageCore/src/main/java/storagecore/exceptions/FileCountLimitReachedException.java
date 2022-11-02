package storagecore.exceptions;

public class FileCountLimitReachedException extends RuntimeException {
    public FileCountLimitReachedException() {
        super("You've reached the maximum amount of files that can be in a directory");
    }
}
