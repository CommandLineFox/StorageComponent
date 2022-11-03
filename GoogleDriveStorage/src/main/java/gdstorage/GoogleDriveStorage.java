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
import com.google.api.services.drive.model.FileList;
import storagecore.StorageCore;
import storagecore.enums.ConfigItem;



import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GoogleDriveStorage(String root) {super(root);}

    public GoogleDriveStorage(String root, int maxSizeLimit, List<String> bannedExtensions, int fileCountLimit) {
        super(root, maxSizeLimit, bannedExtensions, fileCountLimit);
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
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    //Kod za pretragu
    public static void main(String[] args) throws IOException {

        Drive service = getDriveService();

      /*  FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }*/



        try {
            // File's metadata.
            File fileMetadata = new File();
            fileMetadata.setName("config.json");
            fileMetadata.setParents(Collections.singletonList("appDataFolder"));
            java.io.File filePath = new java.io.File("proba.json");
            FileContent mediaContent = new FileContent("application/json", filePath);
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to create file: " + e.getDetails());
            throw e;
        }


    }

    @Override
    public void enterDirectory(String s) {

    }

    @Override
    public void returnBackFromDirectory() {

    }

    @Override
    public void createDirectory(String s) throws FileAlreadyExistsException {

        File file = new File();
        file.setName(s);
        file.setMimeType("application/vnd.google-apps.folder");
        file.setParents(Collections.singletonList(getRoot()));


        try {
            file = GoogleDriveStorage.getDriveService().files().create(file)
                    .setFields("id,parents")
                    .execute();
            System.out.println("New Root ID: " + file.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }

       // setRoot(file.getId()); //POSTAVLJEN NOVI ROOT kretanje



    }

    @Override
    public void createDirectory(int i, int i1) throws FileAlreadyExistsException {


        for(int b=i;b<=i1;b++)
        {
            File file = new File();
            file.setName(String.valueOf(b));
            file.setMimeType("application/vnd.google-apps.folder");
            file.setParents(Collections.singletonList(getRoot()));


            try {
                file = GoogleDriveStorage.getDriveService().files().create(file)
                        .setFields("id,parents")
                        .execute();
                System.out.println("New Root ID: " + file.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }

          //  setRoot(file.getId()); //POSTAVLJEN NOVI ROOT   kretanje

        }
    }

    @Override
    public void createDirectory(String s, int i, int i1) throws FileAlreadyExistsException {

        for(int b=i;b<=i1;b++)
        {
            File file = new File();
            file.setName(s+" "+b);
            file.setMimeType("application/vnd.google-apps.folder");
            file.setParents(Collections.singletonList(getRoot()));


            try {
                file = GoogleDriveStorage.getDriveService().files().create(file)
                        .setFields("id,parents")
                        .execute();
                System.out.println("New Root ID: " + file.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //  setRoot(file.getId()); //POSTAVLJEN NOVI ROOT   kretanje

        }

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

