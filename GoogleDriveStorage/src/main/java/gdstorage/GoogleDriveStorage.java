package gdstorage;

import storagecore.StorageCore;

import java.util.Date;
import java.util.List;

public class GoogleDriveStorage extends StorageCore {
    public GoogleDriveStorage(String root) {
        super(root);
    }

    public GoogleDriveStorage(String root, int maxSizeLimit, List<String> bannedExtensions, int fileCountLimit) {
        super(root, maxSizeLimit, bannedExtensions, fileCountLimit);
    }

    @Override
    public void enterDirectory(String s) {

    }

    @Override
    public void returnBackFromDirectory() {

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