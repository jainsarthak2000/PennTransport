package edu.upenn.cis350.penntransport;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {
    private boolean pennEast;
    private boolean pennWest;
    private boolean lucyGreen;
    private boolean lucyGold;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        //gets whether they were checked before or not
        lucyGold = getIntent().getBooleanExtra("Lucy Gold", true);
        lucyGreen = getIntent().getBooleanExtra("Lucy Green", true);
        pennWest = getIntent().getBooleanExtra("Penn West", true);
        pennEast = getIntent().getBooleanExtra("Penn East", true);
        email = getIntent().getStringExtra("email");

        //updates each switch based on what they were already displaying
        Switch lucyGreenSwitch = (Switch) findViewById(R.id.lucyGreenSwitch);
        lucyGreenSwitch.setChecked(lucyGreen);

        Switch lucyGoldSwitch = (Switch) findViewById(R.id.lucyGoldSwitch);
        lucyGoldSwitch.setChecked(lucyGold);

        Switch pennEastSwitch = (Switch) findViewById(R.id.pennBusEastSwitch);
        pennEastSwitch.setChecked(pennEast);

        Switch pennWestSwitch = (Switch) findViewById(R.id.pennBusWestSwitch);
        pennWestSwitch.setChecked(pennWest);
    }

    public void onClick(View v) {
        // initiate a Switch
        Switch lucyGreenSwitch = (Switch) findViewById(R.id.lucyGreenSwitch);
        // check current state of a Switch (true or false).
        lucyGreen = lucyGreenSwitch.isChecked();

        // initiate a Switch
        Switch lucyGoldSwitch = (Switch) findViewById(R.id.lucyGoldSwitch);
        // check current state of a Switch (true or false).
        lucyGold = lucyGoldSwitch.isChecked();

        // initiate a Switch
        Switch pennEastSwitch = (Switch) findViewById(R.id.pennBusEastSwitch);
        // check current state of a Switch (true or false).
        pennEast =  pennEastSwitch.isChecked();

        // initiate a Switch
        Switch pennWestSwitch = (Switch) findViewById(R.id.pennBusWestSwitch);
        // check current state of a Switch (true or false).
        pennWest =  pennWestSwitch.isChecked();

        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("Penn East", pennEast);
        i.putExtra("Penn West", pennWest);
        i.putExtra("Lucy Green", lucyGreen);
        i.putExtra("Lucy Gold", lucyGold);

        /**
         * Also put the user's email so they stay logged in
         */
        i.putExtra("Email", email);

        startActivityForResult(i,1);
    }

}
