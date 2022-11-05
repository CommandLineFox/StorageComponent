package storagecore;

public class StorageManager {
    private static StorageCore storage = null;

    public static void register(StorageCore stor) {
        storage = stor;
    }

    public static StorageCore getStorage(String root) {
        storage.setRoot(root);
        return storage;
    }
}
