package localstorage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import storagecore.StorageCore;
import storagecore.enums.ConfigItem;
import storagecore.exceptions.BannedExtensionUploadException;
import storagecore.exceptions.FileCountLimitReachedException;
import storagecore.exceptions.MaxSizeLimitBreachedException;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

public class LocalStorage extends StorageCore {
    public LocalStorage(String root) {
        super(root);
    }

    public LocalStorage(String root, int maxSizeLimit, List<String> bannedExtensions, int fileCountLimit) {
        super(root, maxSizeLimit, bannedExtensions, fileCountLimit);
    }

    @Override
    protected void updateConfig(int maxSizeLimit, List<String> bannedExtensions, int fileCountLimit) {
        try {
            FileWriter fileWriter = new FileWriter(getRoot() + "\\config.json");
            JSONObject json = new JSONObject();
            json.put("max_size_limit", maxSizeLimit);
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(bannedExtensions);
            json.put("banned_extensions", jsonArray);
            json.put("file_count_limit", fileCountLimit);
            fileWriter.write(json.toString());
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Object readConfig(ConfigItem configItem) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new FileReader(getRoot() + "\\config.json"));
            switch (configItem) {
                case BANNED_EXTENSIONS -> {
                    return json.get("banned_extensions");
                }
                case FILE_COUNT_LIMIT -> {
                    return json.get("file_count_limit");
                }
                case MAX_SIZE_LIMIT -> {
                    return json.get("max_size_limit");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void enterDirectory(String name) {
        String path = getRoot() + "\\" + name;
        File f = new File(getRoot(), name);
        if (f.exists() && f.isDirectory()) {
            setRoot(path);
            System.out.println("Root has been set to " + path);
        }
    }

    @Override
    public void returnBackFromDirectory() {
        String path = getRoot().substring(getRoot().lastIndexOf('\\'));
        setRoot(path);
    }

    @Override
    public void createDirectory(String name) throws FileAlreadyExistsException, FileCountLimitReachedException {
        int amount = new File(getRoot()).list().length;
        if (amount == getFileCountLimit()) {
            throw new FileCountLimitReachedException();
        }

        File file = new File(getRoot(), name);
        if (file.exists()) {
            throw new FileAlreadyExistsException("Provided directory " + name + " already exists.");
        }

        boolean check = file.mkdir();
        if (check) {
            System.out.println("Successfully created directory" + name);
        } else {
            System.out.println("Something went wrong when creating the directory");
        }
    }

    @Override
    public void createDirectory(int start, int end) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCount(getRoot());

        for (int i = start; i <= end; i++) {
            String name = String.valueOf(i);
            createDirectory(name);
        }
    }

    @Override
    public void createDirectory(String name, int start, int end) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCount(getRoot());

        for (int i = start; i <= end; i++) {
            createDirectory(name + i);
        }
    }

    @Override
    public void addFile(String file) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException, MaxSizeLimitBreachedException {
        File original = new File(file);
        String name = file.substring(file.lastIndexOf("\\") + 1);
        File root = new File(getRoot(), name);

        if (!original.exists()) {
            throw new FileNotFoundException("Couldn't find the file or directory you're looking for");
        }

        if (root.exists()) {
            throw new FileAlreadyExistsException("The file already exists in the destination");
        }

        checkFileCount(root.toPath().toString());
        checkBannedExtension(original.getName());
        checkMaxSizeLimimt(original);

        try {
            Files.copy(original.toPath(), root.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFileOrFolder(String name) throws FileNotFoundException {
        File file = new File(getRoot(), name);
        if (!file.exists()) {
            throw new FileNotFoundException("Couldn't find the file you're looking for");
        }

        boolean result = file.delete();
        if (result) {
            System.out.println("Successfully deleted the file or directory");
        } else {
            System.out.println("There was a problem when deleting");
        }
    }

    @Override
    public void moveFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {
        File original = new File(getRoot(), name);
        File root = new File(path, name);

        if (!original.exists()) {
            throw new FileNotFoundException("Couldn't find the file or directory you're looking for");
        }

        if (root.exists()) {
            throw new FileAlreadyExistsException("The file already exists in the destination");
        }

        checkFileCount(root.toPath().toString());

        try {
            Files.move(original.toPath(), root.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException {
        moveFileOrDirectory(name, path);
    }

    @Override
    public void renameFileOrDirectory(String name, String newName) throws FileNotFoundException, FileAlreadyExistsException {
        File file = new File(getRoot(), name);
        File rename = new File(getRoot(), newName);

        if (!file.exists()) {
            throw new FileNotFoundException("Couldn't find the file you're looking for");
        }

        if (rename.exists()) {
            throw new FileAlreadyExistsException("File with specified name already exists");
        }

        boolean result = file.renameTo(rename);
        if (result) {
            System.out.println("Successfully renamed the file or directory");
        } else {
            System.out.println("There was a problem when deleting");
        }
    }

    @Override
    public void searchByName(String name) {

    }

    @Override
    public void searchByExtension(String extension) {

    }

    @Override
    public void searchByModifiedAfter(Date date) {

    }

    private void checkFileCount(String file) throws FileNotFoundException, FileCountLimitReachedException {
        File root = new File(file);
        if (!root.exists()) {
            throw new FileNotFoundException("Couldn't find the root");
        }

        int amount = root.list().length;
        if (amount == getFileCountLimit()) {
            throw new FileCountLimitReachedException();
        }
    }

    private void checkBannedExtension(String name) throws BannedExtensionUploadException {
        String extension = name.substring(name.lastIndexOf("." + 1)).trim();
        if (getBannedExtensions().contains(extension)) {
            throw new BannedExtensionUploadException();
        }
    }

    private void checkMaxSizeLimimt(File file) throws FileNotFoundException, MaxSizeLimitBreachedException {
        File root = new File(getRoot());
        if (!root.exists()) {
            throw new FileNotFoundException("Couldn't find the root");
        }

        if (getFolderSize(root) + file.length() > getMaxSizeLimit()) {
            throw new MaxSizeLimitBreachedException();
        }
    }

    private int getFolderSize(File root) {
        int length = 0;
        File[] files = root.listFiles();
        if (files != null) {
            int count = files.length;

            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += getFolderSize(file);
                }
            }
        }

        return length;
    }
}