package edu.upenn.cis350.penntransport;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
/*
This actvity handles calculating the routes- we keep track of the origin and destination and find
ot which route should be taken. Should there be a requirement to change stops, we also keep track
of where to get off mid way, where to go to catch the next bus, which bus route to take,
and the final stop.

 */

public class NavigationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //keeps track of value selected via spinner for origin
    String origin = "";
    // MAYA- Call fucntion to find closest time
    //keeps track of value selected via spinner for destination
    String destination= "";
    //CALL
    //keeps track of the route that is determined
    String whatRoute= "";
    //returns true if a bus chnage is required
    boolean changeRoute;
    //what stop to get off at to change routes mid way
    String changeGetOff= "";
    //what stop to get on after changing routes
    String changeGetOnForDest= "";
    // what bus to get on after changing routes
    String changeBus= "";

    private HashMap<String, HashMap<Integer, String>> lucyGreenTimes;
    private HashMap<String, HashMap<Integer, String>> lucyGoldTimes;
    private HashMap<String, HashMap<Integer, String>> pennEastTimes;
    private HashMap<String, HashMap<Integer, String>> pennWestTimes;


    Spinner originSpinner, destinationSpinner;
    TextView originText, destinationText, changeText, getOffFirst, getOnNext;
    Button nextRoute;

    // hashmap<String(Bus Stop), HashMap<String(Time)>, Integer(converted to minutes)>) for each bus key- stops and the vlaue hasmarray of the times - perhaps convert to minutes?

    //HASHMAP CALLED LUCY GREEN
    //KEY - BUS STOP
    //VALUE - HasMap<String time, int minutes>

    //EG: 38th and spruce current time is 4: 15
    //I want ot convert 4: 15 into minutes 60 * 4 + 15
    // Go to hashmap - iterate through the values of the minutes of  38th & spruce VALUE - HasMap<String time, int minutes>
    //find nearest time - return string value associated with that

//    String[] lucyGreenStops = {"38th St. & Spruce St.","33rd St. & Spruce St.","33rd St. & Walnut St"};
//
//    String[] lucyGoldStops = {"30th St. & JFK Blvd. (arrive)","34th St. & Market St.","34th St and Chestnut St",
//            "34th St and Walnut St","40th St. & Walnut St. (UPenn)","40th St and Chestnut St",
//            "Presbyterian Medical Center","37th St and Market St"};
//
//    String[] pennBusEast = {"The Quad","Pottruck Gym","David Rittenhouse Labs (DRL)"};
//
//    //pennbus west does NOT have DRL
//    String[] pennBusWest = {"Franklin's Table","Penn Bookstore","Schattner Building (Dental School)"};

    // Maya here
    // Updated COVID stops.
    String[] lucyGreenStops = {"38th St. & Spruce St.",
            "33rd St. & Spruce St.","33rd St. & Walnut St"};

    String[] lucyGoldStops = {"30th St. & JFK Blvd. (arrive)","34th St. & Market St.",
            "34th St. and Spruce St","40th St. & Walnut St. (UPenn)",
            "Presbyterian Medical Center"};

    //PEN BUS WEST HAS DRL!
    String[] pennBusWest = {"Penn Bookstore",
            "40th St. & Walnut St. (UPenn)", "David Rittenhouse Labs (DRL)"};

    String[] pennBusEast = {"The Quad","Pottruck Gym",
            "Franklin's Table", "Schattner Building (Dental School)"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        originSpinner = findViewById(R.id.originSpinner);
        destinationSpinner = findViewById(R.id.destinationSpinner);

        originText = findViewById(R.id.originText);
        destinationText = findViewById(R.id.destinationText);
        changeText = findViewById(R.id.changeText);
        getOffFirst = findViewById(R.id.getOffFirst);
        getOnNext = findViewById(R.id.getOnNext);

        nextRoute = findViewById(R.id.nextStop);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle!= null) {
            lucyGreenTimes = (HashMap<String, HashMap<Integer, String>>)
                    bundle.getSerializable("LucyGreen Times");
            lucyGoldTimes = (HashMap<String, HashMap<Integer, String>>)
                    bundle.getSerializable("LucyGold Times");
            pennWestTimes = (HashMap<String, HashMap<Integer, String>>)
                    bundle.getSerializable("PennWest Times");
            pennEastTimes = (HashMap<String, HashMap<Integer, String>>)
                    bundle.getSerializable("PennEast Times");
        } else {
            System.out.println("help! bundle is null again!");
        }

    }
    // THE FOLLOWING FUNCTIONS CHECK WHAT ROUTE THE STOP IS IN
    public boolean isInGoldLoop(String stop){
        for(int i = 0; i < lucyGoldStops.length; i++){
            if(lucyGoldStops[i].contentEquals(stop)){
                return true;
            }
        }
        return false;
    }

    public boolean isInGreenLoop(String stop){
        for(int i = 0; i < lucyGreenStops.length; i++){
            if(lucyGreenStops[i].contentEquals(stop)){
                return true;
            }
        }
        return false;
    }

    public boolean isInEast(String stop){
        for(int i = 0; i < pennBusEast.length; i++){
            if(pennBusEast[i].contentEquals(stop)){
                return true;
            }
        }
        return false;
    }

    public boolean isInWest(String stop){
        for(int i = 0; i < pennBusWest.length; i++){
            if(pennBusWest[i].contentEquals(stop)){
                return true;
            }
        }
        return false;
    }
    // THIS FUNCTIONS DETERMINES THE ROUTE AND ANY BUS CHANGES REQUIRED
    public void generateRoute(){
        // if both are in lucyGo, we till take lucyGO
        if(isInGoldLoop(origin) && isInGoldLoop(destination)){
            whatRoute = "LUCYGO";
            changeRoute = false;
            changeGetOff = "";
            changeGetOnForDest = "";
            changeBus = "";
        }
        //LUCYGO and LUCYGREEN have the same bus route, LUCYGREEN just has extra so if 1 is in
        //green and other in gold or both in green, we want ot take LUCYGO
        else if((isInGoldLoop(origin) && isInGreenLoop(destination)) ||
                (isInGreenLoop(origin) && isInGoldLoop(destination)) ||
                (isInGreenLoop(origin) && isInGreenLoop(destination)) ){
            whatRoute = "LUCYGREEN";
            changeRoute = false;
            changeGetOff = "";
            changeGetOnForDest = "";
            changeBus = "";
        }

        // if both are in WEST, we till take WEST
        else if(isInWest(origin) && isInWest(destination)){
            whatRoute = "Penn Bus WEST";
            changeRoute = false;
            changeGetOff = "";
            changeGetOnForDest = "";
            changeBus = "";
        }
        //EAST and WEST have the same bus route, WEST JUST HAS EXTRA so if 1 is in
        //E and other in W or both in E, we want ot take E

        // Maya here: EXCEPT when we start form dental school when that is in East
        else if((isInEast(origin) && isInEast(destination)) ){
            System.out.println("Yes");
            whatRoute = "Penn Bus EAST";
            changeRoute = false;
            changeGetOff = "";
            changeGetOnForDest = "";
            changeBus = "";
        }
        else if((isInWest(origin) && isInWest(destination)) ){

            whatRoute = "Penn Bus WEST";
            changeRoute = false;
            changeGetOff = "";
            changeGetOnForDest = "";
            changeBus = "";
        }
       //DIYA DONE
        else if ((isInWest(origin) && isInEast(destination)))
        {
            if(origin.equals("Franklin's Table")){
                whatRoute = "Penn Bus EAST";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else if (destination.equals("Franklin's Table")){
                whatRoute = "Penn Bus WEST";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            }

            else {
                whatRoute = "Penn Bus WEST";
                changeRoute = true;
                changeGetOff = "Franklin's Table";
                changeGetOnForDest = "Franklin's Table";
                changeBus = "Penn Bus EAST";
            }
        }
        //DIYA DONE
        else if ((isInEast(origin) && isInWest(destination))){
            if(origin.equals("Franklin's Table")){
                whatRoute = "Penn Bus WEST";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else if (destination.equals("Franklin's Table")){
                whatRoute = "Penn Bus EAST";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            }
            else {
                whatRoute = "Penn Bus EAST";
                changeRoute = true;
                changeGetOff = "Franklin's Table";
                changeGetOnForDest = "Franklin's Table";
                changeBus = "Penn Bus WEST";
            }
        }



        // if looking for routes between origin is PennBusE and destination is GREEN LOOP
        //DIYA DONE
        else if(isInEast(origin) && isInGreenLoop(destination)){
            if(destination.equals("38th St. & Spruce St.")){
                whatRoute = "Penn Bus EAST";
                destination = "The Quad, 38th St. & Spruce St. is across the street";

                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            }
            else {
                whatRoute = "Penn Bus EAST";
                changeRoute = true;
                changeGetOff = "the Quad";
                changeGetOnForDest = "38th St. & Spruce St., which is across the street";
                changeBus = "LUCYGREEN";
            }

        }


        //MAYA TO DO
        // if looking for routes between origin is PennBusE and destination is Gold LOOP
        else if(isInEast(origin) && isInGoldLoop(destination)){
            if(origin.equals("The Quad")){
                //origin = "38th St. & Spruce St.";
                origin = "38th St. & Spruce St., which is across the street from the Quad";
                //MAYA, this shoud have 38th St. & Spruce St., which is across the street from the Quad
                whatRoute = "LUCYGOLD";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else {
                whatRoute = "Penn Bus EAST";
                changeRoute = true;
                changeGetOff = "The Quad";
                changeGetOnForDest = "38th St. & Spruce St., which is across the street";

                changeBus = "LUCYGOLD";

            }
        }

        //maya TODO
        // if looking for routes between origin is PennBusW and destination is GREEN LOOP
        else if(isInWest(origin) && isInGreenLoop(destination)){
          if(destination.equals("38th St. & Spruce St.")){
                whatRoute = "Penn Bus West";
               //destination = "The Quad";
              destination = "The Quad. 38th St. & Spruce St. is across the street";
               //Maya, this should be The Quad. 38th St. & Spruce St. is across the street
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            }
            else {
                whatRoute = "Penn Bus WEST";
                changeRoute = true;
                changeGetOff = "the Quad";
                changeGetOnForDest = "38th St. & Spruce St., which is across the street";
                changeBus = "LUCYGREEN";
            }

        }

        // if looking for routes between origin is PennBusW and destination is Gold LOOP
        else if(isInWest(origin) && isInGoldLoop(destination)){
            if(origin.equals("The Quad")){
                origin = "38th St. & Spruce St.";
                whatRoute = "LUCYGOLD";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else {
                whatRoute = "Penn Bus WEST";
                changeRoute = true;
                changeGetOff = "The Quad";
                changeGetOnForDest = "38th St. & Spruce St., which is across the street";

                changeBus = "LUCYGOLD";

            }
        }

        //FIXED HERE FINAL- MAYA TODO MODIFY
        //GREEN HAS 38th and Spruce, East has the Quad
        // if looking for routes between origin is GREEN and destination is PennBusE
        else if(isInGreenLoop(origin) && isInEast(destination)){
            if(origin.equals("38th St. & Spruce St.")){

                System.out.println("YES1");
                //origin = "The Quad";
                origin = "the Quad, which is across the street";
                //Maya, this should be the Quad, which is across the street
                whatRoute = "Penn Bus East";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else if(destination.equals("The Quad")){
                System.out.println("YES2");
                whatRoute = "LUCYGREEN";
                changeRoute = false;
                //destination = "38th St. & Spruce St.";
                destination = "38th St. & Spruce St. The Quad is across the street";
                //This should be 38th St. & Spruce St. The quad is across the street

                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else {
                whatRoute = "LUCYGREEN";
                changeRoute = true;
                changeGetOff = "38th St. & Spruce St.";
                changeGetOnForDest = "The Quad";
                changeBus = "Penn Bus East";
            }
        }

        // if looking for routes between origin is GOLD and destination is PennBusE

        else if(isInGoldLoop(origin) && isInEast(destination)){
            if(origin.equals("38th St. & Spruce St.")){

                System.out.println("YES1");
                //origin = "The Quad";
                origin = "the Quad, which is across the street";
                //Maya, this should be the Quad, which is across the street
                whatRoute = "Penn Bus East";
                changeRoute = false;
                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else if(destination.equals("The Quad")){
                System.out.println("YES2");
                whatRoute = "LUCYGOLD";
                changeRoute = false;
                //destination = "38th St. & Spruce St.";
                destination = "38th St. & Spruce St. The Quad is across the street";
                //This should be 38th St. & Spruce St. The quad is across the street

                changeGetOff = "";
                changeGetOnForDest = "";
                changeBus = "";
            } else {
                whatRoute = "LUCYGOLD";
                changeRoute = true;
                changeGetOff = "38th St. & Spruce St.";
                changeGetOnForDest = "The Quad";
                changeBus = "Penn Bus East";
            }

        }

        // if looking for routes between origin is GREEN and destination is PennBusW

        else if(isInGreenLoop(origin) && isInWest(destination)){
            whatRoute = "Penn Bus GREEN";
            changeRoute = true;
            changeGetOff = "40th and Walnut";
            changeGetOnForDest = "40th and Walnut";
            changeBus = "Penn Bus West";

        }


        // if looking for routes between origin is GOLD  and destination is PennBusW

        else if(isInGoldLoop(origin) && isInWest(destination)){
            whatRoute = "LUCYGOLD";
            changeRoute = true;
            changeGetOff = "40th and Walnut";
            changeGetOnForDest = "40th and Walnut";
            changeBus = "Penn Bus West";

        }
        if(origin.equals("Select Location") || destination.equals("Select Location")){
            originText.setText(("You have not selected the origin / destination. Please select a valid " +
                    "origin and destination and try again." ));
            changeText.setText("");
            destinationText.setText("");
        }

        else if(origin.equals(destination)){
            originText.setText(("You have entered the same location as your origin and destination. " +
                    "\n Please change your selections and try again." ));
            changeText.setText("");
            destinationText.setText("");
        }
        //WRITING WHAT WILL BE DISPLAYED
        //IF NO CHANGE IN ROUTES WE WILL DO AS FOLLOWS-
        else if(!changeRoute) {
            // the only need to do - find the time for origin of the whatRoute
            // same  thing for destination
            nextRoute.setVisibility(View.INVISIBLE);

            // find pickup time
            LocalTime currentTime = LocalTime.now();
            System.out.println(currentTime.toString());
            int hour = currentTime.getHour();
            int minute = currentTime.getMinute();
            int currentTimeMinute = (hour * 60) + minute;

            String originCopy = origin;
            if (origin.equalsIgnoreCase("38th St. & Spruce St., which is across the street from the Quad")) {
                originCopy = "38th St. & Spruce St.";
            } else if (origin.equalsIgnoreCase("the Quad, which is across the street")) {
                originCopy = "The Quad";
            }

            String pickup = findNextTime(currentTimeMinute, whatRoute, originCopy);



            // find dropoff time


            originText.setText(("Get on " + whatRoute + " at the following Stop - " + origin + " at " + pickup));
            changeText.setText("You will not be required to change routes.");
            getOffFirst.setText("");
            getOnNext.setText("");
            destinationText.setText("Get off " + whatRoute + " at " +
                    destination);

            // if we have to change routes
        } else {
            nextRoute.setVisibility(View.VISIBLE);
            // find pickup time
            LocalTime currentTime = LocalTime.now();
            System.out.println(currentTime.toString());
            int hour = currentTime.getHour();
            int minute = currentTime.getMinute();
            int currentTimeMinute = (hour * 60) + minute;

            String originCopy = origin;
            if (origin.equalsIgnoreCase("38th St. & Spruce St., which is across the street from the Quad")) {
                originCopy = "38th St. & Spruce St.";
            } else if (origin.equalsIgnoreCase("the Quad, which is across the street")) {
                originCopy = "The Quad";
            }

            String pickup = findNextTime(currentTimeMinute, whatRoute, originCopy);

            originText.setText(("Get on " + whatRoute + " at the following Stop - " + origin) + " at " + pickup);
//            changeText.setText("You WILL be required to change routes. \n Get off " + whatRoute +
//                    " at " + changeGetOff + ". \n Take the next " + changeBus + " " +
//                    "departing from " + changeGetOnForDest + "."  );

            changeText.setText("You WILL be required to change routes.");

            getOffFirst.setText("Get off " + whatRoute + " at " + changeGetOff +".");
            getOnNext.setText("Take the next " + changeBus + " departing from " + changeGetOnForDest + ".");


            destinationText.setText("Get off " + changeBus + " at the following Stop - " +
                    destination+".");
        }
    }

    public String findNextTime(int time, String route, String place) {
        System.out.println(route);
        System.out.println(place);
        HashMap<Integer, String> desiredTime;
        if (route.toLowerCase().contains("go")) {
            System.out.println(lucyGoldTimes == null);
            desiredTime = lucyGoldTimes.get(place);
            System.out.println("gold");
        } else if (route.toLowerCase().contains("green")) {
            desiredTime = lucyGreenTimes.get(place);
            System.out.println("green");
        } else if (route.toLowerCase().contains("east")) {
            desiredTime = pennEastTimes.get(place);
            System.out.println("place " + place);
            System.out.println(pennEastTimes.containsKey(place));
            System.out.println("TEST!!! Printing the pennEast locations.");
            for (String s : pennEastTimes.keySet()) {
                System.out.println(s);
            }
            System.out.println("east");
        } else { // whatRoute contiains West
            if (place.equals("40th St. & Walnut St. (UPenn)")) {
                place = "40th and Walnut Streets";
            }
            desiredTime = pennWestTimes.get(place);
            System.out.println("west");
        }

        int firstTimeInt = 0;
        String firstTimeString = "";

        for (Map.Entry<Integer, String> mapElement : desiredTime.entrySet()) {
            firstTimeInt = mapElement.getKey();
            firstTimeString = mapElement.getValue();
            break;
        }


        // previous time values
        ArrayList<Integer> orderedTimes = new ArrayList<>();
        int savedInt = firstTimeInt;
        String savedString = firstTimeString;
        for (Map.Entry<Integer, String> mapElement : desiredTime.entrySet()) {
            int currentTime = mapElement.getKey();
            String currentString = mapElement.getValue();
            System.out.println("right now, current time is " + currentString);
            orderedTimes.add(currentTime);
        }

        Collections.sort(orderedTimes);
        for (Integer currTime : orderedTimes) {
            if (time < currTime) {
                savedInt = currTime;
                savedString = desiredTime.get(currTime);
                break;
            }
        }

        return savedString;
    }

    // WHAT TO DO WHEN THE GENERATE ROUTE BUTTON IS PRESSED
    public void onNavClick(View view){
        origin = originSpinner.getSelectedItem().toString();
        System.out.println(origin);
        destination = destinationSpinner.getSelectedItem().toString();
        generateRoute();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onNextStopClick(View view) {
        LocalTime currentTime = LocalTime.now();
        System.out.println(currentTime.toString());
        int hour = currentTime.getHour();
        int minute = currentTime.getMinute();
        int currentTimeMinute = (hour * 60) + minute;

        String nextBus = changeBus;
        String nextDest = changeGetOnForDest;

        if (nextDest.contains("the Quad")) {
            nextDest = "The Quad";
        } else if (nextDest.contains("40th and Walnut")) {
            nextDest = "40th St. & Walnut St. (UPenn)";
        } else if (nextDest.contains("38th St. & Spruce St.")) {
            nextDest = "38th St. & Spruce St.";
        }

        String pickup = findNextTime(currentTimeMinute, nextBus, nextDest);

        getOnNext.setText("Take the next " + changeBus + " departing from " + changeGetOnForDest +
                " at " + pickup);

    }
}
