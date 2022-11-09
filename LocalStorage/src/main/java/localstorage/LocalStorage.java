package localstorage;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import storagecore.StorageCore;
import storagecore.StorageManager;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class LocalStorage extends StorageCore {
    private String originalRoot;

    static {
        StorageManager.register(new LocalStorage());
    }

    private String getOriginalRoot() {
        return originalRoot;
    }

    private void setOriginalRoot(String originalRoot) {
        this.originalRoot = originalRoot;
    }

    @Override
    public boolean checkConfig(String root) {
        File config = new File(root, "config.json");
        return config.exists();
    }

    @Override
    public boolean checkRoot(String root) {
        File file = new File(root);
        return file.exists();
    }

    @Override
    public boolean createRoot(String root) {
        File file = new File(root);
        boolean check = file.mkdirs();
        if (check) {
            setRoot(root);
            setOriginalRoot(root);
            return true;
        } else {
            return false;
        }
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
    public boolean enterDirectory(String name) {
        String path = getRoot() + "\\" + name;
        File f = new File(getRoot(), name);
        if (f.exists() && f.isDirectory()) {
            setRoot(path);
            return true;
        }

        return false;
    }

    @Override
    public boolean returnBackFromDirectory() {
        String path = getRoot().substring(getRoot().lastIndexOf('\\'));
        setRoot(path);
        return true;
    }

    @Override
    public boolean createDirectory(String name) throws FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCountLimit(getRoot());

        File file = new File(getRoot(), name);
        if (file.exists()) {
            throw new FileAlreadyExistsException("Provided directory " + name + " already exists.");
        }

        return file.mkdir();
    }

    @Override
    public boolean createDirectory(String name, int limit) throws FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCountLimit(getRoot());

        if (createDirectory(name)) {
            HashMap fileCountLimits = getFileCountLimits();
            fileCountLimits.put(getRoot() + "\\" + name, limit);
            updateConfig();
            return true;
        }

        return false;
    }

    @Override
    public boolean createDirectory(int start, int end) throws FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCountLimit(getRoot());

        for (int i = start; i <= end; i++) {
            String name = String.valueOf(i);
            if (!createDirectory(name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createDirectory(String name, int start, int end) throws FileAlreadyExistsException, FileCountLimitReachedException {
        checkFileCountLimit(getRoot());

        for (int i = start; i <= end; i++) {
            if (!createDirectory(name + i)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addFile(String file) throws FileAlreadyExistsException, FileCountLimitReachedException, MaxSizeLimitBreachedException {
        File original = new File(file);
        String name = file.substring(file.lastIndexOf("\\") + 1);
        File root = new File(getRoot(), name);

        if (root.exists()) {
            throw new FileAlreadyExistsException("The file already exists in the destination");
        }

        checkFileCountLimit(root.toPath().toString());
        checkBannedExtension(original.getName());
        checkMaxSizeLimit(original.length());

        try {
            Files.copy(original.toPath(), root.toPath());
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteFileOrFolder(String name) throws FileNotFoundException {
        File file = new File(getRoot(), name);
        if (!file.exists()) {
            throw new FileNotFoundException("Couldn't find the file you're looking for");
        }

        return file.delete();
    }

    @Override
    public boolean moveFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitReachedException {
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
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean downloadFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException {
        File original = new File(getRoot(), name);
        File root = new File(path, name);

        if (path.contains(getRoot())) {
            return false;
        }

        if (!original.exists()) {
            throw new FileNotFoundException("Couldn't find the file or directory you're looking for");
        }

        if (root.exists()) {
            throw new FileAlreadyExistsException("The file already exists in the destination");
        }

        checkFileCountLimit(root.toPath().toString());

        try {
            Files.copy(original.toPath(), root.toPath());
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean renameFileOrDirectory(String name, String newName) throws FileNotFoundException, FileAlreadyExistsException {
        File file = new File(getRoot(), name);
        File rename = new File(getRoot(), newName);

        if (!file.exists()) {
            throw new FileNotFoundException("Couldn't find the file you're looking for");
        }

        if (rename.exists()) {
            throw new FileAlreadyExistsException("File with specified name already exists");
        }

        return file.renameTo(rename);
    }

    @Override
    public List<String> searchByName(String name) {
        List<String> result = new ArrayList<>();

        File root = new File(getRoot());
        File[] files = root.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
            if (fileName.equalsIgnoreCase(name)) {
                result.add(file.toPath().toString());
            }
        }

        return result;
    }

    @Override
    public List<String> searchByExtension(String extension) {
        List<String> result = new ArrayList<>();

        File root = new File(getRoot());
        File[] files = root.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            if (fileExtension.equalsIgnoreCase(extension)) {
                result.add(file.toPath().toString());
            }
        }

        return result;
    }

    @Override
    public List<String> searchByModifiedAfter(Date date) {
        List<String> result = new ArrayList<>();

        File root = new File(getRoot());
        File[] files = root.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            Date lastModified = new Date(file.lastModified());
            if (lastModified.compareTo(date) >= 0) {
                result.add(file.toPath().toString());
            }
        }

        return result;
    }

    @Override
    public List<String> searchAllFromRoot(String root) {
        List<String> result = new ArrayList<>();

        File rootFiles = new File(root);
        File[] files = rootFiles.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            result.add(file.toPath().toString());
        }

        return result;
    }

    @Override
    public List<String> searchAllFromRootWithoutRoot(String root) {
        List<String> result = new ArrayList<>();

        File rootFiles = new File(root);
        File[] files = rootFiles.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isFile() && !root.equals(getRoot())) {
                result.add(file.toPath().toString());
            }

            result.addAll(searchAllFromRootWithoutRoot(file.toPath().toString()));
        }

        return result;
    }

    @Override
    public List<String> searchAll() {
        List<String> result = new ArrayList<>();
        result.addAll(searchAllFromRoot(getRoot()));
        result.addAll(searchAllFromRootWithoutRoot(getRoot()));

        return result;
    }

    @Override
    public List<String> searchByPartOfName(String substring) {
        List<String> result = new ArrayList<>();

        File root = new File(getRoot());
        File[] files = root.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
            if (fileName.toLowerCase().contains(substring.toLowerCase())) {
                result.add(file.toPath().toString());
            }
        }

        return result;
    }

    @Override
    protected void checkFileCountLimit(String file) throws FileCountLimitReachedException {
        File root = new File(file);

        if (root.list() == null) {
            return;
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
    protected void checkMaxSizeLimit(double size) throws MaxSizeLimitBreachedException {
        File root = new File(getRoot());

        if (getFolderSize(root) + size > getMaxSizeLimit()) {
            throw new MaxSizeLimitBreachedException();
        }
    }

    private int getFolderSize(File root) {
        int length = 0;
        File[] files = root.listFiles();
        if (files != null) {
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