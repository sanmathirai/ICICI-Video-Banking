import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;

import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.text.Format;

public class CalendarQuickstart {

  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
  private static final List < String > SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  private static ArrayList < String > startEndTime = new ArrayList < String > ();
  private static String formattedUserCurrentTime = "";
  private static String currentUserDate = "";
  private static String formattedCurrentTime = "";
  /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    InputStream in =CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

  public static void main(String...args) throws IOException,
  GeneralSecurityException,
  ParseException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
    String isoDateFormat = "";
    String calendarType = "";
    System.out.println("Please choose ");
    System.out.println("1 Get available slots of Primary Calendar");
    System.out.println("2 Get available slots of Other's Calendar");
    Scanner sc = new Scanner(System. in );
    int option = sc.nextInt();
    JSONObject obj = new JSONObject();
    switch (option) {
    case 1:
      try {
        isoDateFormat = stringToIso();
      } catch(ParseException e) {
        System.out.println("Exception caught");
      }
      calendarType = "primary";
      try {
        getEvents(service, calendarType, isoDateFormat);
      } catch(IOException e) {
        System.out.println("Exception caught");
      } catch(ParseException e) {
        System.out.println("Exception caught");
      }
      claculateFreeSlots(formattedUserCurrentTime, currentUserDate, formattedCurrentTime);
      break;

    case 2:
      System.out.println("Enter email id :");
      calendarType = sc.next();
      try {
        isoDateFormat = stringToIso();
      } catch(ParseException e) {
        System.out.println("Exception caught");
      }
      getEvents(service, calendarType, isoDateFormat);
      claculateFreeSlots(formattedUserCurrentTime, currentUserDate, formattedCurrentTime);
      break;
    }
  }

  public static void getEvents(Calendar service, String calendarType, String isoDateFormat) throws IOException,
  ParseException {
    List < Event > items = null;
    DateTime userDate;
    Events events = null;

    userDate = new DateTime(isoDateFormat);
    events = service.events().list(calendarType).setMaxResults(10).setTimeMin(userDate).setOrderBy("startTime").setSingleEvents(true).execute();
    items = events.getItems();
    //  System.out.println(items);
    if (items.isEmpty()) {
      System.out.println("No upcoming events found.");
    } else {
      System.out.println("Upcoming events");
      for (Event event: items) {
        DateTime start = event.getStart().getDateTime();
        DateTime end = event.getEnd().getDateTime();
        if (start == null) {
          start = event.getStart().getDate();
          end = event.getEnd().getDate();
        }
        //System.out.printf("%s \n", event.getSummary());
        System.out.printf("%s %s \n", start, end);
        convertDate(start, end, userDate);
      }
    }
  }
/*
  converts given date yyyy-MM-dd'T'HH:mm:ss.SSSXXX to dd-MM-yyy format
*/
  public static void convertDate(DateTime start, DateTime end, DateTime userDate) throws ParseException {
    DateTime now = new DateTime(System.currentTimeMillis());
    String startSlot = start.toString();
    String endSlot = end.toString();
    String userDateTime = userDate.toString();
    String today = now.toString();

    String formattedDate = "";

    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy");
    SimpleDateFormat outputTimeFormat = new SimpleDateFormat("h:mm a");

    Date date = inputFormat.parse(startSlot);
    Date endDate = inputFormat.parse(endSlot);
    Date currentUserTime = inputFormat.parse(userDateTime);
    Date todaysDate = inputFormat.parse(today);

    formattedDate = dateFormat.format(date); //formatting event date
    currentUserDate = dateFormat.format(currentUserTime); //formatting current date
    String formattedStartTime = outputTimeFormat.format(date); //formatting event Start time
    String formattedEndTime = outputTimeFormat.format(endDate); //formatting event end time
    formattedUserCurrentTime = outputTimeFormat.format(currentUserTime); //formatting current time  
    formattedCurrentTime = outputTimeFormat.format(todaysDate);

    //System.out.println(formattedDate + " " + " From " + formattedStartTime + " to " + formattedEndTime);
    if (formattedDate.equals(currentUserDate)) {
      startEndTime.add(formattedStartTime);
      startEndTime.add(formattedEndTime);
    }
  }
  /*
    Calculates available slots from calendar based on booked dates
  */
  public static void claculateFreeSlots(String formattedUserCurrentTime, String currentUserDate, String formattedCurrentTime) throws ParseException {

    System.out.println("\n-- List of Free slots on " + currentUserDate + " --");

    if (startEndTime.size() == 0) {
      System.out.println("All slots are free");
    }

    for (int i = 0; i < startEndTime.size(); i++) {
      if (i == 0) {
        System.out.println("12:00 AM" + " - " + startEndTime.get(i));
      } else {
        if (i != startEndTime.size() - 1) {
          System.out.println(startEndTime.get(i) + " - " + startEndTime.get(i + 1));
          i = i + 1;
        }
        else {
          System.out.println(startEndTime.get(startEndTime.size() - 1) + " - " + "11:55 PM");
        }
      }
    }
  }
  /*
  converts given date frmat dd-mm-yyyy to ISO format yyyy-MM-dd'T'HH:mm:ss.SSSXXX
  */
  public static String stringToIso() throws ParseException {
    Scanner sc = new Scanner(System. in );
    System.out.println("Enter Date (dd-MM-yyyy:");
    String inputDate = sc.next();
    SimpleDateFormat input = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    Date date = input.parse(inputDate);
    String formatedIsodate = output.format(date);
    return formatedIsodate;
  }
}