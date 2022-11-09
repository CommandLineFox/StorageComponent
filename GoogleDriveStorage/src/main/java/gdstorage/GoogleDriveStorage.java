package gdstorage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import storagecore.StorageCore;
import storagecore.StorageManager;
import storagecore.enums.ConfigItem;
import storagecore.exceptions.FileCountLimitReachedException;
import storagecore.exceptions.MaxSizeLimitBreachedException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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


    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */

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

    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveStorage.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("/client_secret.json")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }


    //Kod za pretragu
    public static void main(String[] args) throws IOException {


        try {

            Drive service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            File fileMetadata = new File();
            fileMetadata.setName("Ime");
            // fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            // System.out.println("File ID: " + file.getId());
            //file.getId();


        } catch (GeneralSecurityException generalSecurityException) {
            generalSecurityException.printStackTrace();
        }
    }


    Drive service;

    @Override
    public boolean checkConfig(String s) {
        FileList result = null;
        try {
            service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + "config.json" + "'")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                return true;
              /*  if(file.getParents().contains(getRoot()))
                {
                    System.out.println(file.getName());

                }*/
            }

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean checkRoot(String s) {

        FileList result = null;
        try {

            service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            return false;
        } else {

            for (File file : files) {
                if (s.equals(file.getId())) {
                    setRoot(file.getId());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean createRoot(String s) {

        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(s);
            // fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            setRoot(file.getId());

            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void updateConfig() {
        try {
            // File's metadata.
            String json = setConfigJson();
            File fileMetadata = new File();
            Path tempor = Files.createTempFile("config", ".json");
            Files.write(tempor, json.getBytes(StandardCharsets.UTF_8));
            fileMetadata.setName("config.json");
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/json");
            FileContent mediaContent = new FileContent("application/json", tempor.toFile());
            File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();
        } catch (IOException e) {
            System.err.println("ERROR IN UPGRADE CONFIG");

        }
    }

    @Override
    protected Object readConfig(ConfigItem configItem) {


        return null;
    }

    @Override
    public boolean enterDirectory(String s) {

        String dc = convertNameToId(s);
        if (!dc.isEmpty()) {
            setRoot(dc);
            return true;
        }
        return false;
    }

    @Override
    public boolean returnBackFromDirectory() {

        //Ne moze da vrati nazad
        return false;
    }

    @Override
    public boolean createDirectory(String s) throws FileAlreadyExistsException {
        try {

            File fileMetadata = new File();
            fileMetadata.setName(s);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            // System.out.println("File ID: " + file.getId());
            //file.getId();
            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createDirectory(String s, int i) throws FileAlreadyExistsException, FileCountLimitReachedException {
        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(s);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();

            // getFileCountLimits().put(file.getId(),i);
            // updateConfig();

        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createDirectory(int i, int i1) throws FileAlreadyExistsException {


        for (int b = i; b <= i1; b++) {
            if (!createDirectory(Integer.toString(b))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createDirectory(String s, int i, int i1) throws FileAlreadyExistsException {


        for (int b = i; b <= i1; b++) {
            if (!createDirectory(s + b)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addFile(String s) throws FileAlreadyExistsException {

        try {

            File fileMetadata = new File();
            List<String> nizs = List.of(s.split("\\\\"));
            fileMetadata.setName(nizs.get(nizs.size() - 1));
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            java.io.File filePath = new java.io.File(s);
            FileContent mediaContent = new FileContent("application/octet-stream", filePath);
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();


        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteFileOrFolder(String s) throws FileNotFoundException {

        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(s);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            service.files().delete(convertNameToId(s))
                    .setFields("id")
                    .execute();

        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;

    }

    @Override
    public boolean moveFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        File file = null;
        try {
            file = service.files().get(convertNameToId(s))
                    .setFields("parents")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {
            previousParents.append(parent);
            previousParents.append(',');
        }
        // Move the file to the new folder
        try {
            file = service.files().update(s, null)
                    .setAddParents(convertNameToId(s1))
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, parents")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean downloadFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {


        try {

            OutputStream outputStream = new ByteArrayOutputStream();

            service.files().get(s).executeMediaAndDownloadTo(outputStream);
            java.io.File f = new java.io.File(s);
            FileWriter fileWriter = new FileWriter(f.getPath());
            fileWriter.write(String.valueOf(outputStream));
            fileWriter.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean renameFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        FileList result = null;
        try {
            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + s + "'")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                files.get(0).setName(s1);
                service.files().update(convertNameToId(s), files.get(0))
                        .setFields("id")
                        .execute();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> searchByName(String s) {

        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {
            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + s + "'")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                lista.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<String> searchByExtension(String s) {

        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {

            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("fullText contains '" + s + "'")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                lista.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;


    }

    @Override
    public List<String> searchByModifiedAfter(Date date) {

        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {

            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("\tmodifiedTime > '" + date + "'")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                lista.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
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
        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {

            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                lista.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<String> searchByPartOfName(String s) {
        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {

            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("fullText contains '" + s + "'")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                lista.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    protected void checkFileCountLimit(String s) throws FileCountLimitReachedException {

    }

    @Override
    protected void checkMaxSizeLimit(double v) throws MaxSizeLimitBreachedException {

    }

    public String convertNameToId(String s) {
        FileList result = null;
        try {
            result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + s + "'")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && files.size() == 1) {

                //System.out.println(files.get(0).getId());
                return files.get(0).getId();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

