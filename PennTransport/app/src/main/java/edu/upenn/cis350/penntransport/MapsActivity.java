package edu.upenn.cis350.penntransport;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

//import com.twilio.sdk.TwilioRestClient;
import com.google.protobuf.compiler.PluginProtos;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Class: MapsActivity
 *
 * MapsActivity is the Activity that shows the logged-in user the map with the routes.  Users can
 * toggle which routes they would like to see.  There is an SOS button that, when clicked, prompts
 * users to confirm if they would like to send a message to their emergency contact.  There is a
 * profile button that, when clicked, opens a view with the user's profile information.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {
    private GoogleMap mMap;
    private boolean drawPennEast = true;
    private boolean drawPennWest = true;
    private boolean drawLucyGold = true;
    private boolean drawLucyGreen = true;
    private Button navigationButton;

    FirebaseFirestore db =  FirebaseFirestore.getInstance();;
    private static final String TAG = MapsActivity.class.getSimpleName();

    private FusedLocationProviderClient fusedLocationClient;

    String email;
    String name;
    String lastName;
    String emergencyMobile;
    String phoneNumber;
    String emergencyMessage;


    public static final String ACCOUNT_SID = "AC0de9b4a951e7a02ad6ec2a320b30bce7";
    public static final String AUTH_TOKEN = "252553562d1ea3092c95c5364c7069eb";

    private OkHttpClient mClient = new OkHttpClient();

    private HashMap<String, TreeMap<Integer, String>> lucyGreenTimes;
    private HashMap<String, TreeMap<Integer, String>> lucyGoldTimes;
    private HashMap<String, TreeMap<Integer, String>> pennEastTimes;
    private HashMap<String, TreeMap<Integer, String>> pennWestTimes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //please please please LET ME RUN MY APP
        SharedPreferences googleBug = getSharedPreferences("google_bug", Context.MODE_PRIVATE);
        if (!googleBug.contains("fixed")) {
            File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
            corruptedZoomTables.delete();
            googleBug.edit().putBoolean("fixed", true).apply();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /**
         * Sets up clicking on the navigation button
         */
        navigationButton = (Button) findViewById(R.id.navigateButton);
        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNavigationActivity();
            }
        });

        /**
         * Obtain the SupportMapFragment and get notified when the map is ready to be used.
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**
         * Code that needs to run before any other FireBase functions are run
         */
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        /**
         * Fetch the current user's email address so we can access their other data
         */
        email = getIntent().getStringExtra("Email");
        getUserInfo();

        /**
         * Fetch and store the times data so that they can be sent to Navigation
         */
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            lucyGreenTimes = (HashMap<String, TreeMap<Integer, String>>)
                    bundle.getSerializable("LucyGreen Times");
            lucyGoldTimes = (HashMap<String, TreeMap<Integer, String>>)
                    bundle.getSerializable("LucyGold Times");
            pennWestTimes = (HashMap<String, TreeMap<Integer, String>>)
                    bundle.getSerializable("PennWest Times");
            pennEastTimes = (HashMap<String, TreeMap<Integer, String>>)
                    bundle.getSerializable("PennEast Times");
        } else {
            System.out.println("help! bundle is null");
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(getIntent().getExtras() != null) {
            drawLucyGold = getIntent().getBooleanExtra("Lucy Gold", true);
            drawLucyGreen = getIntent().getBooleanExtra("Lucy Green", true);
            drawPennWest = getIntent().getBooleanExtra("Penn West", true);
            drawPennEast = getIntent().getBooleanExtra("Penn East", true);
        }
    }



    /**
     * Gets the emergency mobile number and name of the current user so that, if needed, the app can
     * send a text to the emergency contact.
     */
    private void getUserInfo() {
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    emergencyMobile = document.getString("emergency_mobile");
                    name = document.getString("first_name");
                    lastName = document.getString("last_name");
                    phoneNumber = document.getString("mobile");
                    //update the user button to say first name
                    Button mUserButton = (Button) findViewById(R.id.userDetails);
                    mUserButton.setText(name);
                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });
    }


    /**
     * Function that is run when the filter button is clicked.  Sends the current displayed routes
     * to the new Activity.
     * @param v
     */
    public void onFilterLaunchClick(View v) {
        Intent i = new Intent(this, FilterActivity.class);
        i.putExtra("Penn East", drawPennEast);
        i.putExtra("Penn West", drawPennWest);
        i.putExtra("Lucy Green", drawLucyGreen);
        i.putExtra("Lucy Gold", drawLucyGold);

        /**
         * Also send over the current user's email so that they stay logged in
         */
        i.putExtra("email", email);
        startActivityForResult(i,1);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
//        googleMap.setIndoorEnabled(false); //???

        mMap = googleMap;
        LatLng pennsylvania = new LatLng(41, -77);
        mMap.addMarker(new MarkerOptions().position(pennsylvania).title("Center in Philly"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pennsylvania));

        moveCameraToWantedArea();
        // draw all polylines.
        drawAllPolyLines();

        // Allows app to access current location
        enableLocation();
    }

    private final static int MY_PERMISSION_FINE_LOCATION = 101;

    /**
     * Function that is called when the user first logs in.  It will request location sharing
     * permission from the user.
     */
    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_FINE_LOCATION);
            }
        }

    }

    private final static int MY_PERMISSION_CALL_PHONE = 102;
    Intent intent = new Intent(Intent.ACTION_CALL);

    public void emergencyCall() {
        // Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + emergencyMobile));
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSION_CALL_PHONE);
            }
        }
    }

    /**
     * Function that is invoked after the user indicated their permissions preference.  If they
     * enable location sharing, then they will proceed to the app.  If not, then they will be
     * kicked off the app with a message displaying that they must enable location services.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "This app requires location permissions " +
                            "to be granted.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case MY_PERMISSION_CALL_PHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.CALL_PHONE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "This app requires calling permissions " +
                                "to be granted.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    break;
                }
        }
    }

    /**
     * Bound values for camera focus on app start.
     * BOUND1 is the relative coordinates of the bottom-left corner of the bounded area on the map.
     * BOUND2 is the relative coordinates of the top-right corner of the bounded area on the map.
     * You can change the bounds if required
     *
     */
    private static final LatLng BOUND1 = new LatLng(39.954154, -75.205789);
    private static final LatLng BOUND2 = new LatLng(39.956473, -75.182894);
    /**
     * Method to move camera to wanted bus area.
     */
    private void moveCameraToWantedArea() {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Set up the bounds coordinates for the area we want the user's viewpoint to be.
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(BOUND1)
                        .include(BOUND2)
                        .build();
                // Move the camera now.
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
            }
        });
    }

    /**
     * Method to draw all poly lines. This will manually draw polylines one by one on the map by calling
     * addPolyline(PolylineOptions) on a map instance. The parameter passed in is a new PolylineOptions
     * object which can be configured with details such as line color, line width, clickability, and
     * a list of coordinates values.
     *
     * R.color.colorPolyLineGold
     * Anything that says LINES_GOLD basically any color, go to Utils class na dmake a new one
     *
     */
    private void drawAllPolyLines() {
        if (drawLucyGreen) {
            drawLucyGreenPolyline();
        }
        if (drawLucyGold) {
            drawLucyGoldPolyline();
        }
        if (drawPennEast) {
            drawPennEastPolyline();
        }
        if (drawPennWest) {
            drawPennWestPolyline();
        }
    }
    private void drawPennEastPolyline() {
        // Add a blue Polyline- PENN BUS EAST. Maybe if toggle pressed do this else
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineBlue)) // Line color.
                .width(Utils.POLYLINE_WIDTH) // Line width.
                .clickable(false) // Able to click or not.
                .addAll(Utils.readEncodedPolyLinePointsFromCSV(this, Utils.LINE_BLUE)));
    }

    private void drawPennWestPolyline () {
        // Add a red Polyline- PENN BUS WEST. Maybe if toggle pressed do this else
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineRED)) // Line color.
                .width(Utils.POLYLINE_WIDTH) // Line width.
                .clickable(false) // Able to click or not.
                .addAll(Utils.readEncodedPolyLinePointsFromCSV(this, Utils.LINE_RED)));
    }
    private void drawLucyGoldPolyline() {
        // Add a gold Polyline- LUCYGOLD
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineGold)) // Line color.
                .width(Utils.POLYLINE_WIDTH) // Line width.
                .clickable(false) // Able to click or not.
                .addAll(Utils.readEncodedPolyLinePointsFromCSV(this, Utils.LINE_GOLD))); // all the whole list of lat lng value pairs which is retrieved by calling helper method readEncodedPolyLinePointsFromCSV.
    }

    private void drawLucyGreenPolyline() {
        // Add green polyline - LUCY GREEN LOOP
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineGREEN)) // Line color.
                .width(Utils.POLYLINE_WIDTH) // Line width.
                .clickable(false) // Able to click or not.
                .addAll(Utils.readEncodedPolyLinePointsFromCSV(this, Utils.LINE_GREEN)));
    }

    /**
     * This function is invoked when the SOS button is clicked.  Users will be asked to confirm if
     * they want to send a text to their emergency contact.
     * If so, then a text will be sent to the emergency contact's mobile number.
     * If not, then the dialog will close.
     * @param v
     */
    public void onSOSClick(View v) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(R.string.confirmSendSOSText).setTitle(R.string.confirmEmergency);
//        builder.setPositiveButton(R.string.textSOS, (dialog, id) -> {
//            /**
//             * User clicked SOS, so a text will be sent to their emergency contact.
//             */
//            sendEmergencyMessage();
//            /**
//             * Once message is sent, a confirmation dialog will appear.
//             */
//        });
//        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
//        });
//        AlertDialog dialog = builder.create();
//
//        dialog.show();
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
//                .setTextColor(getResources().getColor(R.color.sosRed));

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Confirm Emergency");
        alertDialog.setMessage("Would you like to send an emergency message?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                "Emergency Call", (dialog, id) -> emergencyCall());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                "Emergency Text", (dialog, id) -> sendEmergencyMessage());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                "Cancel", (dialog, id) -> {});
        alertDialog.show();
    }

    /**
     * This function is invoked when the "SOS" option on the SOS button is clicked.
     */
    private double currentLat;
    private double currentLong;

    public void sendEmergencyMessage() {
        /**
         * Get the last known location
         */
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        currentLat = location.getLatitude();
                        currentLong = location.getLongitude();
                    }
                });

        /**
         * Create a message to send
         */
        String send = name + " currently feels unsafe.  Current location:" + '\n';
        String url = "http://maps.google.com/?q=" + currentLat + "," + currentLong;
        send += url;

        Log.d(TAG, send);

        /**
         * Send message to emergency mobile : NOT WORKING, not broken but message is not sending
         */
        emergencyMessage = send;
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(emergencyMobile, null, emergencyMessage, null, null);
    }


    /**
     * This function is invoked when the "Overview" button is clicked.  It shifts the camera
     * back t
     * @param v
     */
    public void onOverviewButtonClick(View v) {
        moveCameraToWantedArea();
    }


    public void onUserButtonClick(View v) {
        Intent userIntent = new Intent(this, UserProfileActivity.class);
        Bundle extras = new Bundle();
        extras.putString("extra_email", email);
        extras.putString("extra_name", name);
        extras.putString("extra_lastName", lastName);
        extras.putString("extra_emergency", emergencyMobile);
        extras.putString("extra_phone", phoneNumber);
        userIntent.putExtras(extras);
        startActivity(userIntent);
    }

    /**
     * Function that is run when the navigation button is clicked.  S
     *
     */
    public void openNavigationActivity(){
        Intent i = new Intent(this, NavigationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("PennEast Times", (Serializable) pennEastTimes);
        bundle.putSerializable("PennWest Times", (Serializable) pennWestTimes);
        bundle.putSerializable("LucyGold Times", (Serializable) lucyGoldTimes);
        bundle.putSerializable("LucyGreen Times", (Serializable) lucyGreenTimes);
        i.putExtras(bundle);
        startActivityForResult(i,1);
    }


    public void onReportButtonClick(View v){

        Intent reportIntent = new Intent(this, ReportActivity.class);

        Bundle extras = new Bundle();
        extras.putString("extra_email", email);
        extras.putString("extra_name", name);
        extras.putString("extra_lastName", lastName);
        reportIntent.putExtras(extras);
        startActivity(reportIntent);


    }
}


