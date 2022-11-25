package gdstorage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import storagecore.Config;
import storagecore.PrintableFile;
import storagecore.StorageCore;
import storagecore.StorageManager;
import storagecore.enums.ConfigItem;
import storagecore.exceptions.FileCountLimitMultipleFilesBreachedException;
import storagecore.exceptions.FileCountLimitReachedException;
import storagecore.exceptions.MaxSizeLimitBreachedException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class GoogleDriveStorage extends StorageCore {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "GoogleDriveSK";
    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    private static final String TOKENS_DIRECTORY_PATH="tokens";
    static {
        StorageManager.register(new GoogleDriveStorage());
        try {
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
        InputStream in = GoogleDriveStorage.class.getResourceAsStream("/cs.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();
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



    private String glavni_root;




    @Override
    public boolean checkConfig(String s) {
        FileList result = null;
        try {

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + "config.json" + "' AND '" +glavni_root+ "' in parents")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                setConfig(new Config(getMaxSizeLimit(), getBannedExtensions(), getFileCountLimits()));
                return true;
              /*  if(file.getParents().contains(getRoot()))
                {
                    System.out.println(file.getName());

                }*/
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean checkRoot(String s) {

        FileList result = null;
        try {


            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            return false;
        } else {

            for (File file : files) {
                if (s.equals(file.getId())) {
                    setRoot(file.getId());
                    glavni_root=file.getId();
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

            File file = getDriveService().files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            setRoot(file.getId());
            glavni_root=file.getId();
            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {

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
            fileMetadata.setParents(Collections.singletonList(glavni_root));
            fileMetadata.setMimeType("application/json");
            FileContent mediaContent = new FileContent("application/json", tempor.toFile());



            if(checkConfig(glavni_root))
            {
                try {

                    getDriveService().files().delete(convertNameToId("config.json"))
                            .setFields("id")
                            .execute();


                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

            File file = getDriveService().files().create(fileMetadata, mediaContent).setFields("id").execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();
        } catch (IOException e) {
            System.err.println("ERROR IN UPGRADE CONFIG");

        }
    }

    @Override
    protected Object readConfig(ConfigItem configItem) {


        FileList results = null;
        JSONObject json = null;
        try {
            results = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + "config.json" + "' AND '" +glavni_root+ "' in parents")
                    .execute();

            List<File> files = results.getFiles();

            OutputStream outputStream = new ByteArrayOutputStream();
            getDriveService().files().get(files.get(0).getId()).executeMediaAndDownloadTo(outputStream);

            JSONParser jsonParser = new JSONParser();
            Path tempor = Files.createTempFile("config", ".json");
            Files.write(tempor, outputStream.toString().getBytes(StandardCharsets.UTF_8));
            json = (JSONObject) jsonParser.parse(new FileReader(String.valueOf(tempor)));




        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getConfig(json, configItem);
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

    public boolean proveriuslove(String s)
    {
        //PROVERAVA BROJ F U ODREDJENOM D

        HashMap fileCountLimits = getFileCountLimits();

        int brojfajlova=searchAllFromRoot(getRoot()).size();

        Long broj= (Long) fileCountLimits.get(getRoot());


        if(broj!=null && broj<=brojfajlova)
            return false;

        //PROVERAVA BROJ F
        int brojfilova2=searchAllFromRoot(glavni_root).size();
        int brojfajlova1=searchAllFromRootWithoutRoot(glavni_root).size();
        brojfajlova1=brojfajlova1+brojfilova2;

        if(brojfajlova1>getMaxSizeLimit())
            return false;

        //DA LI TO IME VEC POSTOJI
        int poslednjibrojfilova=searchByName(s).size();

        if(poslednjibrojfilova!=0)
            return false;

        return true;
    }


    @Override
    public boolean createDirectory(String s) throws FileAlreadyExistsException {


        if(!proveriuslove(s))
            return false;

        try {

            File fileMetadata = new File();
            fileMetadata.setName(s);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = getDriveService().files().create(fileMetadata)
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

        if(!proveriuslove(s))
            return false;

        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(s);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = getDriveService().files().create(fileMetadata)
                    .setFields("id")
                    .execute();

            HashMap fileCountLimits = getFileCountLimits();
            //Brisi config.json
            fileCountLimits.put(file.getId(), i);
            updateFileCountLimits(fileCountLimits);
            //file.getId();

            // getFileCountLimits().put(file.getId(),i);
            // updateConfig();
            return true;
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

        //Proverava zabranjene ekstenzije
        List<String> niz= List.of(s.split("\\."));
        System.out.println(niz.get(niz.size()-1));
        if(getBannedExtensions().contains("."+niz.get(niz.size()-1)) || getBannedExtensions().contains(niz.get(niz.size()-1)))
           return false;

        if(!proveriuslove(s))
            return false;

        try {
            File fileMetadata = new File();
            List<String> nizs = List.of(s.split("\\\\"));
            fileMetadata.setName(nizs.get(nizs.size() - 1));
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            java.io.File filePath = new java.io.File(s);
            FileContent mediaContent = new FileContent("application/octet-stream", filePath);
            File file = getDriveService().files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();
            return true;

        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteFileOrFolder(String s) throws FileNotFoundException {

        try {
            HashMap fileCountLimits = getFileCountLimits();
            fileCountLimits.remove(convertNameToId(s));
            updateFileCountLimits(fileCountLimits);


            getDriveService().files().delete(convertNameToId(s))
                    .setFields("id")
                    .execute();
            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;

    }

    @Override
    public boolean moveFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        File file = null;
        try {
            file = getDriveService().files().get(convertNameToId(s))
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
            file = getDriveService().files().update(convertNameToId(s), null)
                    .setAddParents(convertNameToId(s1))
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, parents")
                    .execute();
            System.out.println("Move successfully made.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean downloadFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {


        try {

            OutputStream outputStream = new ByteArrayOutputStream();

            getDriveService().files().get(convertNameToId(s)).executeMediaAndDownloadTo(outputStream);
            java.io.File f = new java.io.File(s1+"\\"+s);
            FileWriter fileWriter = new FileWriter(f.getPath());
            fileWriter.write(String.valueOf(outputStream));
            fileWriter.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean renameFileOrDirectory(String s, String s1) throws FileNotFoundException, FileAlreadyExistsException {

        int poslednjibrojfilova=searchByName(s1).size();

        if(poslednjibrojfilova!=0)
            return false;
        try {
            File file = getDriveService().files().update(convertNameToId(s), null)
                    .setFields("id, name")
                    .set("name",s1)
                    .execute();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> searchByName(String s) {

        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {
            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + s + "' AND '" +getRoot()+ "' in parents")
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

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("fullText contains '" + s + "' AND '" +getRoot()+ "' in parents")
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

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("modifiedTime > '" + date + "' AND '" +getRoot()+ "' in parents")
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

        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("'" +s+ "' in parents")
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
    public List<String> searchAllFromRootWithoutRoot(String s) {
        FileList result = null;

        List<String> lista = new ArrayList<>();
        List<File> resenja=new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setQ("'" +s+ "' in parents AND mimeType = 'application/vnd.google-apps.folder'")
                    .setFields("nextPageToken, files(id, name)")
                    .execute();

            List<File> files = result.getFiles();
            resenja=prodjifile(files);


            for (File file : resenja) {
                lista.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<String> searchAll() {
        FileList result = null;
        List<String> lista = new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setQ("'" +getRoot()+ "' in parents")
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

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name contains '" + s + "' AND '" +getRoot()+ "' in parents")
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
    public List<PrintableFile> returnFileList(List<String> list) {

        List<PrintableFile> printableFiles=new ArrayList<>();
        for(String l :list)
        {
            String s=l.split(" ")[l.split(" ").length-1];
            System.out.println(s);
            FileList results = null;
            try {
                results = getDriveService().files().list()
                        .setQ("'"+glavni_root+"' in parents and name ='"+s+"'")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
                List<File> d=results.getFiles();
                File result= d.get(0);

                String exten="";
                if(result.getFileExtension()==null) {
                    if (result.getName().contains(".")) {
                        List<String> niz = List.of(result.getName().split("\\."));
                        exten = niz.get(niz.size() - 1);
                    }
                }
                else{
                    exten=result.getFileExtension();
                }

                String s2=result.getModifiedTime().toString().split("T")[0];
                Date dateModi=new SimpleDateFormat("yyyy-MM-dd").parse(s2);

                String s1=result.getCreatedTime().toString().split("T")[0];
                Date dateCrei=new SimpleDateFormat("yyyy-MM-dd").parse(s1);

                PrintableFile pf=new PrintableFile(result.getName(),result.getId(),exten,dateCrei,dateModi);
                printableFiles.add(pf);
            } catch (IOException | java.text.ParseException e) {
                e.printStackTrace();
            }
        }
        return printableFiles;
    }

    @Override
    protected void checkFileCountLimit(String s) throws FileCountLimitReachedException {

    }

    @Override
    protected void checkMultipleFileCountLimit(String s, int i) throws FileCountLimitMultipleFilesBreachedException {

    }

    @Override
    protected void checkMaxSizeLimit(double v) throws MaxSizeLimitBreachedException {

    }

    public String convertNameToId(String s) {
        FileList result = null;
        try {
            result = getDriveService().files().list()
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


    public List<File> prodjifile( List<File> files) {

        List<File> ressenja=new ArrayList<>();
        try {
            for (File file : files) {
                FileList result2 = null;
                result2 = getDriveService().files().list()
                        .setQ("'" + file.getId() + "' in parents")
                        .setFields("nextPageToken, files(id, name)")
                        .execute();

                List<File> tren = result2.getFiles();

                for (File t : tren)
                    ressenja.add(t);


                FileList result3 = getDriveService().files().list()
                        .setQ("'" + file.getId() + "' in parents AND mimeType = 'application/vnd.google-apps.folder'")
                        .setFields("nextPageToken, files(id, name)")
                        .execute();

                List<File> tren1 = result3.getFiles();
                List<File> tren2= prodjifile(tren1);
                for (File t : tren2)
                    ressenja.add(t);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return ressenja;
    }
}

