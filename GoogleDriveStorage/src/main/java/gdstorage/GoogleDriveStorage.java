package gdstorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import storagecore.StorageCore;
import storagecore.StorageManager;
import storagecore.enums.ConfigItem;

import com.google.api.client.http.FileContent;
import storagecore.enums.FilterType;
import storagecore.enums.OrderType;
import storagecore.enums.SortType;
import storagecore.exceptions.FileCountLimitReachedException;
import storagecore.exceptions.MaxSizeLimitBreachedException;

public class GoogleDriveStorage extends StorageCore {

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "My project";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            StorageManager.register(new GoogleDriveStorage());
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveStorage.class.getResourceAsStream("/client_secret.json");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setAccessType("offline").build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("noviuser1");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        //credential.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }


    //Kod za pretragu
    public static void main(String[] args) throws IOException {

        Drive service = getDriveService();

/*       //ZA PRETRAGU
        FileList result = service.files().list().setFields("nextPageToken, files(id, name)").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
*/
        //ZA DODAVANJE
        //  System.out.println(System.getProperty("user.dir"));
        try {
            // File's metadata.
            File fileMetadata = new File();
            fileMetadata.setName("config.json");
            fileMetadata.setParents(Collections.singletonList("1amNRP4XaNWzV_Dw36tC-Mvyo42EaSqkE"));
            java.io.File filePath = new java.io.File("Ime");
            FileContent mediaContent = new FileContent("application/json", filePath);
            File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to create file: " + e.getDetails());
            throw e;
        }
    }

    @Override
    public boolean checkConfig(String s) {
        return false;
    }

    @Override
    public boolean checkRoot(String s) {
        return false;
    }

    @Override
    public boolean createRoot(String s) {
        return false;
    }

    @Override
    protected void updateConfig() {

    }

    @Override
    protected Object readConfig(ConfigItem configItem) {
        return null;
    }

    @Override
    public boolean enterDirectory(String s) {

        return false;
    }

    @Override
    public boolean returnBackFromDirectory() {

        return false;
    }

    @Override
    public boolean createDirectory(String s) throws FileAlreadyExistsException {
        File file = new File();
        file.setName(s);
        file.setMimeType("application/vnd.google-apps.folder");
        file.setParents(Collections.singletonList(getRoot()));

        try {
            file = GoogleDriveStorage.getDriveService().files().create(file).setFields("id,parents").execute();
            return true;

        } catch (IOException e) {
            return false;
        }

        // setRoot(file.getId()); //POSTAVLJEN NOVI ROOT kretanje
    }

    @Override
    public boolean createDirectory(String s, int i) throws FileAlreadyExistsException, FileCountLimitReachedException {
        return false;
    }

    @Override
    public boolean createDirectory(int i, int i1) throws FileAlreadyExistsException {
        for (int b = i; b <= i1; b++) {
            File file = new File();
            file.setName(String.valueOf(b));
            file.setMimeType("application/vnd.google-apps.folder");
            file.setParents(Collections.singletonList(getRoot()));

            try {
                file = GoogleDriveStorage.getDriveService().files().create(file).setFields("id,parents").execute();
            } catch (IOException e) {
                return false;
            }

            //  setRoot(file.getId()); //POSTAVLJEN NOVI ROOT   kretanje
        }
        return true;
    }

    @Override
    public boolean createDirectory(String s, int i, int i1) throws FileAlreadyExistsException {
        for (int b = i; b <= i1; b++) {
            File file = new File();
            file.setName(s + " " + b);
            file.setMimeType("application/vnd.google-apps.folder");
            file.setParents(Collections.singletonList(getRoot()));

            try {
                file = GoogleDriveStorage.getDriveService().files().create(file).setFields("id,parents").execute();
                System.out.println("New Root ID: " + file.getId());
            } catch (IOException e) {
                return false;
            }

            //  setRoot(file.getId()); //POSTAVLJEN NOVI ROOT   kretanje
        }
        return true;
    }

    @Override
    public boolean addFile(String s) throws FileAlreadyExistsException {

        return false;
    }

    @Override
    public boolean deleteFileOrFolder(String s) throws FileNotFoundException {

        return false;
    }

    @Override
    public boolean moveFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        return false;
    }

    @Override
    public boolean downloadFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        return false;
    }

    @Override
    public boolean renameFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        return false;
    }

    @Override
    public List<String> searchByName(String s) {

        return null;
    }

    @Override
    public List<String> searchByExtension(String s) {

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
    protected void checkFileCountLimit(String s) throws FileCountLimitReachedException {

    }

    @Override
    protected void checkMaxSizeLimit(double v) throws MaxSizeLimitBreachedException {

    }
}

