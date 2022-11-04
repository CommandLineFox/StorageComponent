package localstorage;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import storagecore.StorageCore;
import storagecore.enums.ConfigItem;
import storagecore.enums.FilterType;
import storagecore.enums.OrderType;
import storagecore.enums.SortType;
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

    public LocalStorage(String root, int maxSizeLimit, List<String> bannedExtensions) {
        super(root, maxSizeLimit, bannedExtensions);
    }

    @Override
    protected boolean checkConfig(String root) {
        File config = new File(root, "config.json");
        return config.exists();
    }

    @Override
    protected void updateConfig() {
        try {
            FileWriter fileWriter = new FileWriter(getRoot() + "\\config.json");
            String json = setConfigJson();
            fileWriter.write(json);
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
            return getConfig(json, configItem);
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
    public void createDirectory(String s, int i) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {

    }

    @Override
    public void createDirectory(int start, int end) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCountLimit(getRoot());

        for (int i = start; i <= end; i++) {
            String name = String.valueOf(i);
            createDirectory(name);
        }
    }

    @Override
    public void createDirectory(String name, int start, int end) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCountLimit(getRoot());

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

        checkFileCountLimit(root.toPath().toString());
        checkBannedExtension(original.getName());
        checkMaxSizeLimit(original.length());

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

        checkFileCountLimit(root.toPath().toString());

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
    public List<String> searchByName(String name) {
        return null;
    }

    @Override
    public List<String> searchByExtension(String extension) {
        return null;
    }

    @Override
    public List<String> searchByModifiedAfter(Date date) {
        return null;
    }

    @Override
    public List<String> searchAllFromRoot(String s) {
        return null;
    }

    @Override
    public List<String> searchAllFromRootWithoutRoot(String s) {
        return null;
    }

    @Override
    public List<String> searchAll() {
        return null;
    }

    @Override
    public List<String> searchByPartOfName(String s) {
        return null;
    }

    @Override
    public List<String> sortResults(List<String> list, SortType sortType, OrderType orderType) {
        return null;
    }

    @Override
    public List<String> filterResults(List<String> list, List<FilterType> list1) {
        return null;
    }

    @Override
    protected void checkFileCountLimit(String file) throws FileNotFoundException, FileCountLimitReachedException {
        File root = new File(file);
        if (!root.exists()) {
            throw new FileNotFoundException("Couldn't find the root");
        }

        if ((Integer) getFileCountLimits().get(root.toPath()) == root.list().length) {
            throw new FileCountLimitReachedException();
        }
    }

    protected void checkBannedExtension(String name) throws BannedExtensionUploadException {
        String extension = name.substring(name.lastIndexOf("." + 1)).trim();
        if (getBannedExtensions().contains(extension)) {
            throw new BannedExtensionUploadException();
        }
    }

    @Override
    protected void checkMaxSizeLimit(double size) throws FileNotFoundException, MaxSizeLimitBreachedException {
        File root = new File(getRoot());
        if (!root.exists()) {
            throw new FileNotFoundException("Couldn't find the root");
        }

        if (getFolderSize(root) + size > getMaxSizeLimit()) {
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