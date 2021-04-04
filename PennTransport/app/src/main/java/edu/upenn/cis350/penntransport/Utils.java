package edu.upenn.cis350.penntransport;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * Default log tag name for log message.
     */
    private static final String LOG_TAG = MapsActivity.class.getName();
    /**
     * Keyword constants for reading values from polylines.csv.
     * Important: these keywords values must be exactly the same as ones in polylines.csv file in raw folder.
     * ANUSHA- Add LINE_GREEN
     * Don't forget to add Line color under the values tab in rres folder
     */
    public static final String ENCODED_POINTS = "encodedPoints";
    public static final String LINE_GOLD = "lineGold";
    public static final String LINE_BLUE = "lineBlue";
    public static final String LINE_RED = "lineRed";
    public static final String LINE_GREEN = "lineGreen";
    public static final Integer POLYLINE_WIDTH = 6;
    public static final String LAT_LNG_POINT = "latLngPoint";
    public static final String MARKER = "marker";
    /**
     * Helper method to get polyline points by decoding an encoded coordinates string read from CSV file.
     */
    static List<LatLng> readEncodedPolyLinePointsFromCSV(Context context, String lineKeyword) {
        // Create an InputStream object.
        InputStream is = context.getResources().openRawResource(R.raw.polylines);
        // Create a BufferedReader object to read values from CSV file.
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        // Create a list of LatLng objects.
        List<LatLng> latLngList = new ArrayList<>();
        try {
            while ((line = reader.readLine()) != null) {
                // Split the line into different tokens (using the comma as a separator).
                String[] tokens = line.split(",");
                // Only add the right latlng points to a desired line by color.
                if (tokens[0].trim().equals(lineKeyword) && tokens[1].trim().equals(ENCODED_POINTS)) {
                    // Use PolyUtil to decode the polylines path into list of LatLng objects.
                    latLngList.addAll(PolyUtil.decode(tokens[2].trim().replace("\\\\", "\\")));
                    Log.d(LOG_TAG + lineKeyword, tokens[2].trim());
                    for (LatLng lat : latLngList) {
                        Log.d(LOG_TAG + lineKeyword, lat.latitude + ", " + lat.longitude);
                    }
                } else {
                    Log.d(LOG_TAG, "null");
                }
            }
        } catch (IOException e1) {
            Log.e(LOG_TAG, "Error" + line, e1);
            e1.printStackTrace();
        }
        return latLngList;
    }

}