package edu.upenn.cis350.penntransport;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;
import java.util.TreeMap;


/**
 * Class: LoginActivity
 *
 * LoginActivity is the Activity that is created when the app starts.  It allows for the
 * user to enter a username and password.  Once these fields are filled in, the user can
 * click "Login" to attempt to log in to the app.  If a correct username and password
 * pair is given, the user can proceed to the MapActivity.  If not, the user will be
 * notified that this is an incorrect pairing.  The user can also click "Register Here"
 * to be redirected to RegisterActivity, where they can create a username and password.
 */
public class LoginActivity extends AppCompatActivity  {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 202;
    EditText username;  // Entered username
    EditText password;  // Entered password

    String usernameString;
    String passwordString;

    String actualPassword;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
// string is bus stops
    // the different times -
    //routes
    static volatile Map<String, ArrayList<String>> pennEastOutput = new HashMap<>();
    static volatile Map<String, ArrayList<String>> pennWestOutput = new HashMap<>();
    static volatile Map<String, ArrayList<String>> lucyGoldOutput = new HashMap<>();
    static volatile Map<String, ArrayList<String>> lucyGreenOutput = new HashMap<>();

    private HashMap<String, TreeMap<Integer, String>> lucyGreenTimes = new HashMap<>();
    private HashMap<String, TreeMap<Integer, String>> lucyGoldTimes = new HashMap<>();
    private HashMap<String, TreeMap<Integer, String>> pennEastTimes = new HashMap<>();
    private HashMap<String, TreeMap<Integer, String>> pennWestTimes = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * This is what is shown when the Activity loads.
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build();
        FirebaseFirestore.setLoggingEnabled(true);

        /**
         * Initialize values for the EditTexts username and password.
         */
        username = findViewById(R.id.usernameText);
        password = findViewById(R.id.passwordText);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        /**
         * lol
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }
        }

        scrapePennEast();
        scrapePennWest();
        scrapeLucyGold();
        scrapeLucyGreen();
//        createTimeMaps();

//        Intent navigationIntent = new Intent(this, NavigationActivity.class);
//        navigationIntent.putExtra("PennEast Times", (Parcelable) pennEastTimes);
//        navigationIntent.putExtra("PennWest Times", (Parcelable) pennWestTimes);
//        navigationIntent.putExtra("LucyGold Times", (Parcelable) lucyGoldTimes);
//        navigationIntent.putExtra("LucyGreen Times", (Parcelable) lucyGreenTimes);
    }

    /**
     * This function is invoked when the user clicks "Register Here".  This function can be
     * invoked regardless of if the user has already entered text into the username and password
     * fields.
     * @param v
     */
    public void onRegisterButtonClick(View v) {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * This function is invoked when the user clicks "Login".  The function will check that the
     * user has entered text for both fields.  If so, the function will check that the pairing is
     * correct.  If so, then the user will be redirected to the Map.
     *
     * If the pairing is not correct, the user will be notified with a Toast that their username
     * or password is incorrect.
     *
     * If there is no text entered in either field, then nothing will happen.
     * --- NOTE: should we prompt the user with another Toast message?
     * @param v
     */
    public void onLoginButtonClick(View v) {
        /**
         * First check if both fields have text in them.
         */
        username = findViewById(R.id.usernameText);
        password = findViewById(R.id.passwordText);

        usernameString = username.getText().toString();
        passwordString = password.getText().toString();

        if (usernameString.length() > 0 && passwordString.length() > 0) {
            findPassword();
        } else {
            displayToast("Please enter your username and password.");
        }
    }

    /**
     * This function finds the password that is paired with the usernameString, if an account
     * linked to the usernameString exists.
     * If an account linked to the usernameString exists and
     */
    private void findPassword() {
        DocumentReference docRef = db.collection("users").document(usernameString);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                /**
                 * Case where account linked to usernameString exists
                 */
                if (document.exists()) {
                    actualPassword = document.getString("password");
                    if (actualPassword.equals(passwordString)) {
                        // We need to send the email + times data
                        Intent mapsIntent = new Intent(this, MapsActivity.class);
                        mapsIntent.putExtra("Email", usernameString);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PennEast Times", (Serializable) pennEastTimes);
                        bundle.putSerializable("PennWest Times", (Serializable) pennWestTimes);
                        bundle.putSerializable("LucyGold Times", (Serializable) lucyGoldTimes);
                        bundle.putSerializable("LucyGreen Times", (Serializable) lucyGreenTimes);
                        mapsIntent.putExtras(bundle);
                        startActivity(mapsIntent);
                        System.out.println("hello");
                        finish(); //closes the app without letting to come back to login activity
                    } else {
                        displayToast("Your password is incorrect.");
                    }
                }

                /**
                 * Case where account linked to usernameString does not exist
                 */
                else {
                    Log.d(TAG, "No such document");
                    displayToast("There is no account associated with this email.");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    /**
     * Given a String, this function displays a Toast with the input String as the message.
     * @param display
     */
    private void displayToast(String display) {
        Context context = getApplicationContext();
        CharSequence text = display;
        int duration = Toast.LENGTH_SHORT;

        Toast t = Toast.makeText(context, text, duration);
        t.show();
    }

    /**
     * Scrapes the time tables online to update the database with the route times
     */
    /*private void scrapeRoutes () {
        ArrayList<HashMap<String, ArrayList<String>>> eastTimes = scrapePennEast();
        scrapePennWest();
        scrapeLucyGold();
        scrapeLucyGreen();
    } */


    public void scrapePennEast () {
        // help
        //new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true);
        //new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build();
        new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();

        //Map<String, ArrayList<String>> pennEastOutput = new HashMap<>();
        Thread downloadThread = new Thread() {
            public void run() {
                    Document document;
                    Elements busStop;
                    Elements names;
                    try {
                        //Get Document object after parsing the html from given url.
                        document = Jsoup.connect("https://cms.business-services.upenn.edu/transportation/schedules/pennbus-east.html").get();
                        String title = document.title(); //Get title
                        //System.out.println("Website Title: " + title);

                        busStop = document.getElementsByClass("ptp-table-list-item ptp-table-list-row");
                        names = document.getElementsByClass("ptp-table-list-item-heading ptp-type-quaternary");

                        ArrayList<HashMap<String, ArrayList<String>>> pennEast = new ArrayList<>();
                        for (int i = 0; i < busStop.size(); i++) {
                            String current = busStop.get(i).text();
                            String currentName = names.get(i).text();
                            current = current.replace(currentName, "");
                            current = current.replace("~", "");
                            current = current.replace("APPROXIMATE Time (on the hour)", "!!");
                            current = current.replace(" ", "");
                            String[] timeArr = current.split("!!");

                            ArrayList<String> list = new ArrayList<>();

                            for (int j = 1; j < timeArr.length; j++) {
                                for (int k = 5; k <= 12; k++) {
                                    String time;
                                    if (k != 12) {
                                        time = "pm";
                                    } else {
                                        time = "am";
                                    }
                                    String toAdd = "" + k;
                                    toAdd += timeArr[j];
                                    toAdd += time;
                                    list.add(toAdd);
                                    //System.out.println(toAdd);
                                }
                                //create an array copy list times and put it with the currentName
                                String[] currentStopTimesArr = new String[list.size()];
                                for (int z = 0; z < currentStopTimesArr.length; z++) {
                                    currentStopTimesArr[z] = list.get(z);
                                }
                                pennEastOutput.put(currentName, list);
                            }

                        }
                        } catch (
                            IOException e) {
                        e.printStackTrace();
                        Log.d("NOT WORKING :(", "grr");
                    }
                    addToFireBase("Penn East", pennEastOutput);
                }

        };
        downloadThread.start();
    }


    public void scrapePennWest() {
        //Map<String, ArrayList<String>> pennEastOutput = new HashMap<>();
        Thread downloadThread = new Thread() {
            public void run() {
                Document document;
                Elements busStop;
                Elements names;
                try {
                    //Get Document object after parsing the html from given url.
                    document = Jsoup.connect("https://cms.business-services.upenn.edu/transportation/schedules/pennbus-west.html").get();
                    String title = document.title(); //Get title
                    //System.out.println("Website Title: " + title);

                    busStop = document.getElementsByClass("ptp-table-list-item ptp-table-list-row");
                    names = document.getElementsByClass("ptp-table-list-item-heading ptp-type-quaternary");

                    ArrayList<HashMap<String, ArrayList<String>>> pennEast = new ArrayList<>();
                    for (int i = 0; i < busStop.size(); i++) {
                        String current = busStop.get(i).text();
                        String currentName = names.get(i).text();
                        current = current.replace(currentName, "");
                        current = current.replace("~", "");
                        current = current.replace("APPROXIMATE Time (on the hour)", "!!");
                        current = current.replace(" ", "");
                        String[] timeArr = current.split("!!");

                        ArrayList<String> list = new ArrayList<>();

                        for (int j = 1; j < timeArr.length; j++) {
                            for (int k = 5; k <= 12; k++) {
                                String time;
                                if (k != 12) {
                                    time = "pm";
                                } else {
                                    time = "am";
                                }
                                String toAdd = "" + k;
                                toAdd += timeArr[j];
                                toAdd += time;
                                list.add(toAdd);
                                //System.out.println(toAdd);
                            }
                            //create an array copy list times and put it with the currentName
                            String[] currentStopTimesArr = new String[list.size()];
                            for (int z = 0; z < currentStopTimesArr.length; z++) {
                                currentStopTimesArr[z] = list.get(z);
                            }
                            pennWestOutput.put(currentName, list);
                        }

                    }
                } catch (
                        IOException e) {
                    e.printStackTrace();
                    Log.d("NOT WORKING :(", "grr");
                }
                addToFireBase("Penn West", pennWestOutput);
            }

        };
        downloadThread.start();
    }

    private void scrapeLucyGold() {
        Thread downloadThread = new Thread() {
            public void run() {
                Document document;
                try {
                    //Get Document object after parsing the html from given url.
                    document = Jsoup.connect("http://www.septa.org/schedules/covid/w/LUCYGO_0a.html").get();
                    String title = document.title(); //Get title
                    //System.out.println("Website Title: " + title);
                    //of form busStopName ----- times.....
                    Elements rows = document.select("tr");
                    Elements busStopNames = document.getElementsByClass("zone");

                    for (int i=0; i < rows.size(); i++) {
                        String row = rows.get(i).text();
                        String stopName = busStopNames.get(i).text();

                        row = row.replace(stopName, "");
                        row = row.replace("—", "");
                        row = row.replace(" ", "");
                        //System.out.println(row);
                        String [] timeArr = row.split("m");

                        ArrayList <String> list = new ArrayList<>();

                        for (int j = 0; j < timeArr.length; j++) {
                            list.add(timeArr[j] + "m");
                        }

                        lucyGoldOutput.put(stopName, list);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("NOT WORKING :(", "grr");
                }
                addToFireBase("Lucy Gold", lucyGoldOutput);
            }

        };
        downloadThread.start();
    }

    private void scrapeLucyGreen() {
        Thread downloadThread = new Thread() {
            public void run() {
                Document document;
                try {
                    //Get Document object after parsing the html from given url.
                    document = Jsoup.connect("http://www.septa.org/schedules/covid/w/LUCYGR_0a.html").get();
                    String title = document.title(); //Get title
                    //System.out.println("Website Title: " + title);
                    //of form busStopName ----- times.....
                    Elements rows = document.select("tr");
                    Elements busStopNames = document.getElementsByClass("zone");

                    for (int i=0; i < rows.size(); i++) {
                        String row = rows.get(i).text();
                        String stopName = busStopNames.get(i).text();

                        row = row.replace(stopName, "");
                        row = row.replace("—", "");
                        row = row.replace(" ", "");
                        //System.out.println(row);
                        String [] timeArr = row.split("m");

                        ArrayList <String> list = new ArrayList<>();

                        for (int j = 0; j < timeArr.length; j++) {
                            list.add(timeArr[j] + "m");
                        }
                        
                        lucyGreenOutput.put(stopName, list);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("NOT WORKING :(", "grr");
                }
                addToFireBase("Lucy Green", lucyGreenOutput);
            }

        };
        downloadThread.start();
    }

    private void addToFireBase(String docName, Map<String, ArrayList<String>> output) {
        db.collection("Bus Routes").document(docName)
                .set(output)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        createTimeMaps();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    /**
     * Converts a time of the format X:XXam or XX:XXam (or pm) to minutes.
     * @param time
     * @return
     */
    private int convertToMinutes(String time) {
        int indexOfColon = time.indexOf(':');
        if (indexOfColon == -1) {
            throw new IllegalArgumentException();
        } else {
            String hour = time.substring(0, indexOfColon);
            String minutes = time.substring(indexOfColon + 1);
            String timeOfDay = minutes.substring(minutes.length() - 2);
            minutes = minutes.substring(0, minutes.length() - 2);
            int hourInt = Integer.parseInt(hour);
            int minuteInt = Integer.parseInt(minutes);
            if (timeOfDay.toLowerCase().equals("pm")) {
                if (hourInt == 12) {
                    // ex. 12:35 PM is represented as 12:35 in military time
                    return (60 * hourInt) + minuteInt;
                } else {
                    // ex. 1:35 PM is represented as 13:35 in military time
                    return (60 * hourInt) + minuteInt + (60 * 12);
                }
            } else if (timeOfDay.toLowerCase().equals("am")) {
                if (hourInt == 12) {
                    // ex. 12:35 AM is represented as 00:35 in military time
                    return minuteInt;
                } else {
                    // ex. 4:00 AM is represented as 04:00 in military time
                    return (60 * hourInt) + minuteInt;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Helper function that creates a mapping of a list of time strings to its minutes representation.
     * Example input: "6:15am, "12:15pm", 3:30pm"
     * Example output: "6:15am" -> 375, "12:15pm" -> 735, "3:30pm" -> 915
     * @param times
     * @return
     */
    private TreeMap<Integer, String> createTimeMapsHelper(ArrayList<String> times) {
        TreeMap<Integer, String> convertedTimes = new TreeMap<>();
        for (String timeString : times) {
            int minutes = convertToMinutes(timeString);
            convertedTimes.put(minutes, timeString);
        }
        return convertedTimes;
    }

    /**
     * Function that creates the time mappings for each bus.
     */
    private void createTimeMaps() {
        System.out.println(lucyGoldOutput.size() + " is the size of lucyGoldOutput");
        System.out.println(lucyGreenOutput.size() + " is the size of lucyGreenOutput");
        System.out.println(pennEastOutput.size() + " is the size of PennEastOutput");
        System.out.println(pennWestOutput.size() + " is the eize of PennWestOutput");

        // iterate through each stop
        for (Map.Entry<String, ArrayList<String>> mapElement: lucyGoldOutput.entrySet()) {
            String stop = mapElement.getKey();
            ArrayList<String> times = mapElement.getValue();
            TreeMap<Integer, String> convertedTimes = createTimeMapsHelper(times);
            lucyGoldTimes.put(stop, convertedTimes);
        }

        for (Map.Entry<String, ArrayList<String>> mapElement: lucyGreenOutput.entrySet()) {
            String stop = mapElement.getKey();
            ArrayList<String> times = mapElement.getValue();
            TreeMap<Integer, String> convertedTimes = createTimeMapsHelper(times);
            lucyGreenTimes.put(stop, convertedTimes);
        }

        for (Map.Entry<String, ArrayList<String>> mapElement: pennEastOutput.entrySet()) {
            String stop = mapElement.getKey();
            ArrayList<String> times = mapElement.getValue();
            TreeMap<Integer, String> convertedTimes = createTimeMapsHelper(times);
            pennEastTimes.put(stop, convertedTimes);
        }

        for (Map.Entry<String, ArrayList<String>> mapElement: pennWestOutput.entrySet()) {
            String stop = mapElement.getKey();
            ArrayList<String> times = mapElement.getValue();
            TreeMap<Integer, String> convertedTimes = createTimeMapsHelper(times);
            pennWestTimes.put(stop, convertedTimes);
        }
    }
}
