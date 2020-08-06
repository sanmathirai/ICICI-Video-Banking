import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DriveQuickstart {
  private static final String APPLICATION_NAME = "Create folder in google drive";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */

  private static final List < String > SCOPES = Collections.singletonList(DriveScopes.DRIVE);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */

  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    InputStream in =DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if ( in ==null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader( in ));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  //Creating new folder
  public static void createFolder(Drive service, File fileMetadata, String folderName) throws IOException {

    fileMetadata.setName(folderName);
    fileMetadata.setMimeType("application/vnd.google-apps.folder");

    File file = service.files().create(fileMetadata).setFields("id").execute();
    System.out.println("\nFolder created successfully");
    System.out.println("Folder ID: " + file.getId());
  }

  //Uploading fie
  public static void uploadFile(Drive service, File fileMetadata, String fileName, String localFilePath, String folderID)
  throws IOException,
  FileNotFoundException {

    fileMetadata.setName(fileName);

    if (folderID != "") {
      fileMetadata.setParents(Collections.singletonList(folderID));
    }
    java.io.File filePath = new java.io.File(localFilePath);
    FileContent mediaContent = new FileContent("application/pdf/video", filePath);
    File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
    System.out.println("File Uploded");
    System.out.println("File ID: " + file.getId());
  }

  public static void listFolder(Drive service, List < File > files) throws IOException {

    if (files == null || files.isEmpty()) {
      System.out.println("No files found.");
    } else {
      System.out.println("Folders:");
      for (File file: files) {
        System.out.printf("%s \n", file.getName());
      }
    }
  }

  public static String getFolderId(String driveFolderName, Drive service, List < File > files) throws IOException {

    String fileId = "";
    System.out.println("Enter folder name to store file");
    String folderName = "FOLDER1";
    for (File file: files) {
      if (file.getName().equals(driveFolderName)) {
        fileId = file.getId();
      }

      break;
    }
    if (fileId == "") {
      System.out.println("Folder Not found.File will be uploaded");
      return "";
    }
    else return fileId;
  }

  public static void main(String[] args) throws IOException,
  GeneralSecurityException {

    int option = 0;
    File fileMetadata = null;
    Drive service = null;
    FileList result = null;
    List < File > files = null;
    Scanner sc = new Scanner(System. in );
    // Build a new authorized API client service.
    try {
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();

      result = service.files().list().setOrderBy("folder").setPageSize(10).setFields("nextPageToken, files(id, name)").execute();
      files = result.getFiles();
      fileMetadata = new File();

    } catch(IOException e) {
      System.out.println("Exception caught");
    }
    catch(GeneralSecurityException e) {
      System.out.println("Exception caught");
    }

    System.out.println("Please choose ");
    System.out.println("Press 1 Create new folder");
    System.out.println("Press 2 Upload File");

    option = sc.nextInt();

    switch (option) {

    case 1:
      System.out.println("Enter Folder name ");
      String folderName = sc.next();

      try {
        createFolder(service, fileMetadata, folderName);
      } catch(IOException e) {
        System.out.println("Exception caught");
      }
      break;
    case 2:
      String folderId = "";
      try {
        listFolder(service, files);
      } catch(ArithmeticException e) {
        System.out.println("Exception caught");
      }
      System.out.println("Enter the folder name for file to be stored");
      String driveFolderName = sc.next();

      try {
        folderId = getFolderId(driveFolderName, service, files);
      } catch(ArithmeticException e) {
        System.out.println("Exception caught");
      }

      System.out.println("Enter File Name");
      String fileName = sc.next();

      System.out.println("Enter File Path");
      String localFilePath = sc.next();

      try {
        uploadFile(service, fileMetadata, fileName, localFilePath, folderId);
      } catch(ArithmeticException e) {
        System.out.println("Exception caught");
      } catch(FileNotFoundException e) {
        System.out.println("invalid filepath");
      }
      break;
    default:
      System.out.println("invalid option");
    }
  }

}