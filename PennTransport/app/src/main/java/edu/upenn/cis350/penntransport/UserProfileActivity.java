package edu.upenn.cis350.penntransport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;


public class UserProfileActivity extends AppCompatActivity {

    TextView mname;
    TextView memail;
    TextView memergency;
    TextView mphone;
    TextView mwelcome;
    String name;
    String lastName;
    String email;
    String emergencyMobile;
    String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        Bundle extras = getIntent().getExtras();


        mname = (TextView) findViewById(R.id.nameUser);
        memail = (TextView) findViewById(R.id.usernameUser);
        memergency = (TextView) findViewById(R.id.emergencyNumberUser);
        mphone = (TextView) findViewById(R.id.phoneNumberUser);
        mwelcome = (TextView) findViewById(R.id.welcomeUser);

        name = extras.getString("extra_name");
        mwelcome.setText("Welcome, " + name + "!");
        lastName = extras.getString("extra_lastName");
        mname.setText("Name: " + name + " " + lastName);

        email = extras.getString("extra_email");
        memail.setText("Email id: " + email);

        emergencyMobile = extras.getString("extra_emergency");
        memergency.setText("Emergency Number: " + emergencyMobile);

        phoneNumber = extras.getString("extra_phone");
        mphone.setText("Phone Number: " + phoneNumber);

        System.out.println(name + lastName + email + emergencyMobile + phoneNumber);

    }

    public void onLogoutButtonClick(View v){
        Intent logOutIntent = new Intent(this, LoginActivity.class);
        logOutIntent.putExtra("finish", true); // if you are checking for this in your other Activities
        logOutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logOutIntent);
        finish();

    }


}
