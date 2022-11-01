package gdstorage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import storagecore.StorageCore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.util.Date;
import java.util.List;

public class GoogleDriveStorage extends StorageCore {

    private static final String APPLICATION_NAME = "GoogleDriveSK";


    private static HttpTransport HTTP_TRANSPORT;


    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
/*
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveStorage.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException

    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
*/
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
    public void createDirectory(String s) throws FileAlreadyExistsException {

    }

    @Override
    public void createDirectory(int i, int i1) throws FileAlreadyExistsException {

    }

    @Override
    public void createDirectory(String s, int i, int i1) throws FileAlreadyExistsException {

    }

    @Override
    public void addFile(String s) throws FileNotFoundException, FileAlreadyExistsException {

    }

    @Override
    public void deleteFileOrFolder(String s) throws FileNotFoundException {

    }

    @Override
    public void moveFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

    }

    @Override
    public void downloadFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

    }

    @Override
    public void renameFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

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