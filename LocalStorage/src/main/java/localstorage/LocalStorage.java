package localstorage;

import storagecore.StorageCore;

import java.io.File;
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
    public void enterDirectory(String s) {
        String path = getRoot() + "/" + s;
        File f = new File(path);
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
    public void createDirectory(String s) {

    }

    @Override
    public void createDirectory(int i, int i1) {

    }

    @Override
    public void createDirectory(String s, int i, int i1) {

    }

    @Override
    public void addFile(String s) {

    }

    @Override
    public void deleteFileOrFolder(String s) {

    }

    @Override
    public void moveFileOrDirectory(String s, String s1) {

    }

    @Override
    public void downloadFileOrDirectory(String s, String s1) {

    }

    @Override
    public void renameFileOrDirectory(String s, String s1) {

    }

    @Override
    public void searchByName(String s) {

    }

    @Override
    public void searchByExtension(String s) {

    }

    @Override
    public void searchByModifiedAfter(Date date) {

    }
}