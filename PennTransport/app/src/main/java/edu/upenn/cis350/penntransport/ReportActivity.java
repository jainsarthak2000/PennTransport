package edu.upenn.cis350.penntransport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Button mDateButton;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    TextView mDateDisplay;

    Button mTimeButton;
    TimePickerDialog.OnTimeSetListener mTimeSetListener;
    TextView mTimeDisplay;

    TextView mname;
    TextView memail;
    String name;
    String lastName;
    String email;

    Spinner originSpinner;
    String origin;
    Spinner routeSpinner;
    String route;

    String date;
    String time;

    EditText problemView;
    String problem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        Bundle extras = getIntent().getExtras();
        mname = (TextView) findViewById(R.id.nameReport);
        memail = (TextView) findViewById(R.id.usernameReport);

        name = extras.getString("extra_name");
        lastName = extras.getString("extra_lastName");
        mname.setText("Name: " + name + " " + lastName);

        email = extras.getString("extra_email");
        memail.setText("Email id: " + email);


        //DATE

        mDateButton = (Button) findViewById(R.id.dateButton);
        mDateDisplay = (TextView) findViewById(R.id.date);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ReportActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                System.out.println(""+ month + "/" + day + "/" + year);

                date = month + "/" + day + "/" + year;
                mDateDisplay.setText("   " + date);
            }
        };

        //TIME


        mTimeButton = (Button) findViewById(R.id.timeButton);
        mTimeDisplay = (TextView) findViewById(R.id.time);

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int currHour = cal.get(Calendar.HOUR_OF_DAY);
                int currMinute = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(
                        ReportActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mTimeSetListener,
                        currHour, currMinute,false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                time = hourOfDay + ":" + minutes;
                mTimeDisplay.setText("   " + hourOfDay + ":" + minutes);
            }
        };
    }

    public void onReportSubmitButtonClick(View v){
        originSpinner = (Spinner) findViewById(R.id.originReportSpinner);
        origin = originSpinner.getSelectedItem().toString();

        routeSpinner = (Spinner) findViewById(R.id.busRouteReportSpinner);
        route = routeSpinner.getSelectedItem().toString();

        problemView = (EditText) findViewById(R.id.problem);
        problem = problemView.getText().toString();


        Map<String, String> report = new HashMap<>();
        report.put("name", name + " " + lastName);
        report.put("email", email);
        report.put("origin", origin);
        report.put("route", route);
        report.put("date", date);
        report.put("time", time);
        report.put("problem", problem);

        addToFireBase("Reports" , report);

        mDateDisplay.setText(null);

        mTimeDisplay.setText(null);

        originSpinner.setSelection(0);;
        origin = null;
        routeSpinner.setSelection(0);
        route = null;

        date = null;
        time = null;

        problemView.getText().clear();;
        problem = null;

        Toast.makeText(getApplicationContext(),"Your report was recorded.",Toast.LENGTH_SHORT).show();

    }

    private void addToFireBase(String docName, Map<String, String> output) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        db.collection("Reports").document("Report from " + email +
                " on " + timestamp)
                .set(output)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error writing document: " + e);
                    }
                });
    }

}
