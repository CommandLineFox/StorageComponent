package storagecore;

import java.util.Date;

public class PrintableFile {
    private final String path;
    private final String name;
    private final String extension;
    private final Date createdAt;
    private final Date lastModifiedAt;

    public PrintableFile(String path, String name, String extension, Date createdAt, Date lastModifiedAt) {
        this.path = path;
        this.name = name;
        this.extension = extension;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }
}
