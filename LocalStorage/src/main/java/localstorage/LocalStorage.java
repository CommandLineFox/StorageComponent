package localstorage;

import storagecore.StorageCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        String path = getRoot().substring(getRoot().lastIndexOf('/'));
        setRoot(path);
    }

    @Override
    public void createDirectory(String name) throws FileAlreadyExistsException {
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
    public void createDirectory(int start, int end) throws FileAlreadyExistsException {
        for (int i = start; i <= end; i++) {
            String name = String.valueOf(i);
            createDirectory(name);
        }
    }

    @Override
    public void createDirectory(String name, int start, int end) throws FileAlreadyExistsException {
        for (int i = start; i <= end; i++) {
            createDirectory(name + i);
        }
    }

    @Override
    public void addFile(String file) throws FileNotFoundException, FileAlreadyExistsException {
        File original = new File(file);
        String name = file.substring(file.lastIndexOf("\\") + 1);
        File root = new File(getRoot(), name);

        if (!original.exists()) {
            throw new FileNotFoundException("Couldn't find the file or directory you're looking for");
        }

        if (root.exists()) {
            throw new FileAlreadyExistsException("The file already exists in the destination");
        }

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
    public void moveFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException {
        File original = new File(getRoot(), name);
        File root = new File(path, name);

        if (!original.exists()) {
            throw new FileNotFoundException("Couldn't find the file or directory you're looking for");
        }

        if (root.exists()) {
            throw new FileAlreadyExistsException("The file already exists in the destination");
        }

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
}