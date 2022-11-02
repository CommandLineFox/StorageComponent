package storagecore;

import storagecore.enums.ConfigItem;
import storagecore.exceptions.FileCountLimitReachedException;
import storagecore.exceptions.MaxSizeLimitBreachedException;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class StorageCore {
    private String root;

    /**
     * Create a storage with default settings
     * Max size limit - 127
     * Banned extensions - none
     * File count limit - 20
     *
     * @param root The root position of the remote storage
     */
    public StorageCore(String root) {
        this.root = root;
        updateConfig(12000, new ArrayList<>(), 20);
    }

    /**
     * Create a storage with custom settings
     *
     * @param root             The root position of the remote storage
     * @param maxSizeLimit     The max size limit of an uploaded file
     * @param bannedExtensions The list of banned extensions that can't be uploaded
     * @param fileCountLimit   The maximum amount of files in a directory
     */
    public StorageCore(String root, int maxSizeLimit, List<String> bannedExtensions, int fileCountLimit) {
        this.root = root;
        updateConfig(maxSizeLimit, bannedExtensions, fileCountLimit);
    }

    /**
     * Get the current root position
     *
     * @return The current root position
     */
    public String getRoot() {
        return root;
    }

    /**
     * Set the current root position
     *
     * @param root The new root position
     */
    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * Get the maximum size limit of an uploaded file
     *
     * @return The limit in bytes
     */
    public int getMaxSizeLimit() {
        return (Integer) readConfig(ConfigItem.MAX_SIZE_LIMIT);
    }

    /**
     * Get the list of banned extensions
     *
     * @return An ArrayList of banned extensions
     */
    public List<String> getBannedExtensions() {
        return (List<String>) readConfig(ConfigItem.BANNED_EXTENSIONS);
    }

    /**
     * Get the maximum amount of files that can be in a directory
     *
     * @return The amount of files as an integer
     */
    public int getFileCountLimit() {
        return (Integer) readConfig(ConfigItem.FILE_COUNT_LIMIT);
    }

    protected abstract void updateConfig(int maxSizeLimit, List<String> bannedExtensions, int fileCountLimit);

    protected abstract Object readConfig(ConfigItem configItem);

    /**
     * Enter a specified directory
     *
     * @param name The name of the directory
     */
    public abstract void enterDirectory(String name);

    /**
     * Return to the previous directory
     */
    public abstract void returnBackFromDirectory();

    /**
     * Create a directory in the current directory
     *
     * @param name The name of the directory
     */
    public abstract void createDirectory(String name) throws FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Create several directories with names in range of given numbers in the current directory
     *
     * @param start The bottom number that the file is named with
     * @param end   The top number that the file is named with
     */
    public abstract void createDirectory(int start, int end) throws FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Create several directories with a prefix and names ending in range of numbers in the current directory
     *
     * @param name  The name prefix
     * @param start The bottom number
     * @param end   The top number
     */
    public abstract void createDirectory(String name, int start, int end) throws FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Move a given file to the storage
     *
     * @param file The path to the file that's being added to the current directory
     */
    public abstract void addFile(String file) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException, MaxSizeLimitBreachedException;

    /**
     * Delete a file or directory at a given path
     *
     * @param name The name of the file or directory
     */
    public abstract void deleteFileOrFolder(String name) throws FileNotFoundException;

    /**
     * Move a file from current directory to a new path
     *
     * @param name The name of the file to move
     * @param path The relative path where the file will be moved to
     */
    public abstract void moveFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Download a file or directory from current directory
     *
     * @param name The name of the file to download
     * @param path The path to place the file in
     */
    public abstract void downloadFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException;

    /**
     * Rename a file or directory in the current directory
     *
     * @param name    The name of the file to rename
     * @param newName The new name
     */
    public abstract void renameFileOrDirectory(String name, String newName) throws FileNotFoundException, FileAlreadyExistsException;

    /**
     * Search a file or directory by its name in the current directory
     *
     * @param name The name of the file or directory
     */
    public abstract void searchByName(String name);

    /**
     * Search a file by its extension in the current directory
     *
     * @param extension The extension to filter by
     */
    public abstract void searchByExtension(String extension);

    /**
     * Search a file or directory by its last modified date being younger than provided date in the current directory
     *
     * @param date The maximum date the results can be old
     */
    public abstract void searchByModifiedAfter(Date date);
}