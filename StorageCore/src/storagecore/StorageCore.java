package storagecore;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class StorageCore {
    private String root;
    private int maxSizeLimit;
    private List<String> bannedExtensions;
    private int fileCountLimit;

    /**
     * Create a storage with default settings
     * Max size limit - 127
     * Banned extensions - none
     * File count limit - 20
     */
    public StorageCore() {
        maxSizeLimit = 127;
        bannedExtensions = new ArrayList<>();
        fileCountLimit = 20;
    }

    /**
     * Create a storage with custom settings
     *
     * @param maxSizeLimit     The max size limit of an uploaded file
     * @param bannedExtensions The list of banned extensions that can't be uploaded
     * @param fileCountLimit   The maximum amount of files in a directory
     */
    public StorageCore(byte maxSizeLimit, List<String> bannedExtensions, int fileCountLimit) {
        this.maxSizeLimit = maxSizeLimit;
        this.bannedExtensions = bannedExtensions;
        this.fileCountLimit = fileCountLimit;
    }

    /**
     * Get the maximum size limit of an uploaded file
     *
     * @return The limit in bytes
     */
    public int getMaxSizeLimit() {
        return (Integer) readConfig("max_size_limit");
    }

    /**
     * Set the maximum size limit of an uploaded file
     *
     * @param maxSizeLimit Amount of bytes the limit will be
     */
    public void setMaxSizeLimit(byte maxSizeLimit) {
        this.maxSizeLimit = maxSizeLimit;
        updateConfig();
    }

    /**
     * Get the list of banned extensions
     *
     * @return An ArrayList of banned extensions
     */
    public List<String> getBannedExtensions() {
        return (List<String>) readConfig("banned_extensions");
    }

    /**
     * Set the list of banned extensions
     *
     * @param bannedExtensions The full list of banned extensions
     */
    public void setBannedExtensions(List<String> bannedExtensions) {
        this.bannedExtensions = bannedExtensions;
        updateConfig();
    }

    /**
     * Add a single extension to the banned list
     *
     * @param extension An extension
     */
    public void addBannedExtension(String extension) {
        if (!bannedExtensions.contains(extension)) {
            this.bannedExtensions.add(extension);
        }
        updateConfig();
    }

    /**
     * Remove a single extension from the banned list
     *
     * @param extension An extension
     */
    public void removeBannedExtension(String extension) {
        bannedExtensions.remove(extension);
        updateConfig();
    }

    /**
     * Get the maximum amount of files that can be in a directory
     *
     * @return The amount of files as an integer
     */
    public int getFileCountLimit() {
        return (Integer) readConfig("file_count_limit");
    }

    /**
     * Set the maximum amount of files that can be in a directory
     *
     * @param fileCountLimit The amount of files
     */
    public void setFileCountLimit(int fileCountLimit) {
        this.fileCountLimit = fileCountLimit;
        updateConfig();
    }

    private void updateConfig() {
        try {
            FileWriter fileWriter = new FileWriter("config.json");
            JSONObject json = new JSONObject();
            json.put("max_size_limit", maxSizeLimit);
            json.put("banned_extensions", bannedExtensions.toArray());
            json.put("file_count_limit", fileCountLimit);
            fileWriter.write(json.toString());
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object readConfig(String item) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new FileReader("config.json"));
            return json.get(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a directory in the current directory
     *
     * @param name The name of the directory
     */
    public abstract void createDirectory(String name);

    /**
     * Create several directories with names in range of given numbers in the current directory
     *
     * @param start The bottom number that the file is named with
     * @param end   The top number that the file is named with
     */
    public abstract void createDirectory(int start, int end);

    /**
     * Create several directories with a prefix and names ending in range of numbers in the current directory
     *
     * @param name  The name prefix
     * @param start The bottom number
     * @param end   The top number
     */
    public abstract void createDirectory(String name, int start, int end);

    /**
     * Move a given file to the storage
     *
     * @param file The path to the file that's being added to the current directory
     */
    public abstract void addFile(String file);

    /**
     * Delete a file or directory at a given path
     *
     * @param name The name of the file or directory
     */
    public abstract void deleteFileOrFolder(String name);

    /**
     * Move a file from current directory to a new path
     *
     * @param name The name of the file to move
     * @param path The relative path where the file will be moved to
     */
    public abstract void moveFileOrDirectory(String name, String path);

    /**
     * Download a file or directory from current directory
     *
     * @param name The name of the file to download
     * @param path The path to place the file in
     */
    public abstract void downloadFileOrDirectory(String name, String path);

    /**
     * Rename a file or directory in the current directory
     *
     * @param name    The name of the file to rename
     * @param newName The new name
     */
    public abstract void renameFileOrDirectory(String name, String newName);

    /**
     * Search a file or directory by it's name in the current directory
     *
     * @param name The name of the file or directory
     */
    public abstract void search(String name);
}