package storagecore;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import storagecore.enums.ConfigItem;
import storagecore.enums.FilterType;
import storagecore.enums.OrderType;
import storagecore.enums.SortType;
import storagecore.exceptions.FileCountLimitReachedException;
import storagecore.exceptions.MaxSizeLimitBreachedException;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public abstract class StorageCore {
    private String root;
    private Config config;

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
        if (checkConfig(root)) {
            System.out.println("Storage already has a configuration");
        } else {
            System.out.println("Generating configuration for storage with default parameters");
            this.config = new Config(16000000, new ArrayList<>());
            updateConfig();
            System.out.println("Successfully generated configuration for storage");
        }

        System.out.println("Storage started");
    }

    /**
     * Create a storage with custom settings
     *
     * @param root             The root position of the remote storage
     * @param maxSizeLimit     The max size limit of an uploaded file
     * @param bannedExtensions The list of banned extensions that can't be uploaded
     */
    public StorageCore(String root, int maxSizeLimit, List<String> bannedExtensions) {
        this.root = root;
        if (checkConfig(root)) {
            System.out.println("Storage already has a configuration");
        } else {
            System.out.println("Generating configuration for storage with given parameters");
            this.config = new Config(maxSizeLimit, bannedExtensions);
            updateConfig();
            System.out.println("Successfully generated configuration for storage");
        }

        System.out.println("Storage started");
    }

    /**
     * Check if a config.json file exists at the provided root
     *
     * @param root The path to the root of the storage
     * @return If the config.json file exists or not
     */
    protected abstract boolean checkConfig(String root);

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
    public HashMap getFileCountLimit() {
        return (HashMap) readConfig(ConfigItem.FILE_COUNT_LIMITS);
    }

    /**
     * Update the json file with new values
     */
    protected abstract void updateConfig();

    protected String setConfigJson() {
        JSONObject json = new JSONObject();
        json.put("max_size_limit", config.getMaxSizeLimit());
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(config.getBannedExtensions());
        json.put("banned_extensions", jsonArray);
        json.put("file_count_limits", new JSONObject(config.getFileCountLimits()));
        return json.toString();
    }

    /**
     * Get a specific item from the config
     *
     * @param configItem The item of choice
     * @return The value of the item
     */
    protected abstract Object readConfig(ConfigItem configItem);

    protected Object getConfig(JSONObject json, ConfigItem configItem) {
        switch (configItem) {
            case BANNED_EXTENSIONS -> {
                return json.get("banned_extensions");
            }
            case FILE_COUNT_LIMITS -> {
                return json.get("file_count_limits");
            }
            case MAX_SIZE_LIMIT -> {
                return json.get("max_size_limit");
            }
            default -> {
                return null;
            }
        }
    }

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
    public abstract void createDirectory(String name) throws
            FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Create a directory in the current directory with a file count limit
     *
     * @param name  The name of the directory
     * @param limit The limit of files and directories
     */
    public abstract void createDirectory(String name, int limit) throws
            FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Create several directories with names in range of given numbers in the current directory
     *
     * @param start The bottom number that the file is named with
     * @param end   The top number that the file is named with
     */
    public abstract void createDirectory(int start, int end) throws
            FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Create several directories with a prefix and names ending in range of numbers in the current directory
     *
     * @param name  The name prefix
     * @param start The bottom number
     * @param end   The top number
     */
    public abstract void createDirectory(String name, int start, int end) throws
            FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException;

    /**
     * Move a given file to the storage
     *
     * @param file The path to the file that's being added to the current directory
     */
    public abstract void addFile(String file) throws
            FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException, MaxSizeLimitBreachedException;

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
     * List all files with matching name in the current directory
     *
     * @param name The name of the file or directory
     */
    public abstract List<String> searchByName(String name);

    /**
     * List all files with matching extension in the current directory
     *
     * @param extension The extension to filter by
     */
    public abstract List<String> searchByExtension(String extension);

    /**
     * Search a file or directory by its last modified date being younger than provided date in the current directory
     *
     * @param date The maximum date the results can be old
     */
    public abstract List<String> searchByModifiedAfter(Date date);

    /**
     * List all files in the given directory
     *
     * @param root The directory to search in
     * @return List of results
     */
    public abstract List<String> searchAllFromRoot(String root);

    /**
     * List all files within directories of given directory
     *
     * @param root The directory to search in
     * @return List of results
     */
    public abstract List<String> searchAllFromRootWithoutRoot(String root);

    /**
     * List all files and sub-files in the current directory
     *
     * @return List of results
     */
    public abstract List<String> searchAll();

    /**
     * List all files which contain a substring in their name in the current directory
     *
     * @param substring The substring to search with
     * @return List of results
     */
    public abstract List<String> searchByPartOfName(String substring);

    /**
     * Sort the list of results
     *
     * @param result    The results to sort
     * @param sortType  The type to sort by
     * @param orderType The order to sort by
     * @return Sorted list of results
     */
    public abstract List<String> sortResults(List<String> result, SortType sortType, OrderType orderType);

    /**
     * Filter the list of results before printing it
     *
     * @param result      The results to filter
     * @param filterTypes The list of values to keep
     * @return Filtered list of results
     */
    public abstract List<String> filterResults(List<String> result, List<FilterType> filterTypes);
}