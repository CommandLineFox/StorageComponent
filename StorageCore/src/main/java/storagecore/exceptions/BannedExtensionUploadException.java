package storagecore.exceptions;

public class BannedExtensionUploadException extends RuntimeException {
    public BannedExtensionUploadException() {
        super("You're not allowed to upload files with this extension");
    }
}
