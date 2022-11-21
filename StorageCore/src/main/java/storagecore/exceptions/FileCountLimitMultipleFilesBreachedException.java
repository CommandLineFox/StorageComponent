package storagecore.exceptions;

public class FileCountLimitMultipleFilesBreachedException extends RuntimeException {
    public FileCountLimitMultipleFilesBreachedException() {
        super("You've reached the maximum amount of files that can be in a directory");
    }
}
