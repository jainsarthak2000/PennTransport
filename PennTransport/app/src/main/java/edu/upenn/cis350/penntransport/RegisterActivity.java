package edu.upenn.cis350.penntransport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Class: RegisterActivity
 *
 * RegisterActivity is the Activity that allows new users to create an account with PENNMOVE.
 * It will prompt users to enter their first and last name, PennID, Penn email address, personal
 * phone number, the name of their emergency contact, and the phone number of their emergency
 * contact.
 *
 * NOTES:
 * Our product spec document says that we will use PennID to verify that the user is a Penn
 * student.  The current implementation does not support this verification feature.
 * All fields are required, and users will be prompted to fill out every field.
 */

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    EditText confirmPassword;
    EditText firstName;
    EditText lastName;
    EditText mobile;
    EditText emergencyMobile;

    String emailString;
    String passwordString;
    String confirmPasswordString;
    String firstNameString;
    String lastNameString;
    String mobileString;
    String emergencyMobileString;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = RegisterActivity.class.getSimpleName();


    /**
     * This is what is shown when the Activity loads.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.userEmailId);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        mobile = findViewById(R.id.mobileNumber);
        emergencyMobile = findViewById(R.id.emergencyNumber);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    /**
     * This function is invoked when the Create Account button is clicked.  It first checks that
     * every field is filled with some value.  Then, it checks that the inputted texts are valid.
     * Then, it creates an account for the user and redirects them to the Map.
     * @param v
     */
    public void onCreateAccountButtonClick(View v) {
        if (!nonEmptyInputs()) {
            Context context = getApplicationContext();
            CharSequence text = "Please complete all fields.";
            int duration = Toast.LENGTH_SHORT;

            Toast t = Toast.makeText(context, text, duration);
            t.show();
            display("Please complete all fields.", "Toast");
        } else {
            String error = error();

            /**
             * If the user has entered their data correctly, their account will be made and
             * they will be redirected to the MapsActivity screen.  If not, then an error
             * message will be shown to them, and they will be prompted to try again.
             */
            if (error.equals("")) {
                createAccount();
            } else {
                /**
                 * Create an AlertDialog that allows user to read what went wrong and
                 * click "OK" to fix the fields
                 */
                display(error, "AlertDialog");
            }
        }

    }

    /**
     * This function checks if the passwords in the password and confirm password fields are
     * the same.
     * @return true if the passwords are the same, false otherwise
     */
    private boolean passwordsSame() {
        return passwordString.equals(confirmPasswordString);
    }

    /**
     * This function checks if there are any errors in the fields that the user inputted.
     * @return a String detailing the errors made by the user.  An empty string if the user
     * has not made any errors.
     */
    private String error() {
        String e = "";
        if (!pennEmail()) {
            e += "invalid Penn email" + '\n';
        }
        if (!passwordsSame()) {
            e += "passwords do not match" + '\n';
        }
        if (!validMobile(mobileString)) {
            e += "invalid mobile number" + '\n';
        }
        if (!validMobile(emergencyMobileString)) {
            e += "invalid emergency mobile number" + '\n';
        }

        /**
         * Remove the last '\n' character before the error message is displayed on screen
         * (and politely asks user to fix the errors)
         */
        if (e.length() > 0) {
            e = e.substring(0, e.length() - 1);
            String polite = "Please address the following:" + '\n';
            e = polite + e;
        }
        return e;
    }

    /**
     * This function checks if all fields are non-empty.
     * @return true if all fields are non-empty, false otherwise
     */
    private boolean nonEmptyInputs() {
        emailString = email.getText().toString();
        passwordString = password.getText().toString();
        confirmPasswordString = confirmPassword.getText().toString();
        firstNameString = firstName.getText().toString();
        lastNameString = lastName.getText().toString();
        mobileString = mobile.getText().toString();
        emergencyMobileString = emergencyMobile.getText().toString();

        return emailString.length() > 0 && passwordString.length() > 0 &&
                confirmPasswordString.length() > 0 && firstNameString.length() > 0 &&
                lastNameString.length() > 0 && mobileString.length() > 0 &&
                emergencyMobileString.length() > 0;
    }

    /**
     * This function checks if the provided email address is a Penn email, that is, if it ends
     * with ".upenn.edu".
     * @return true if the email address is a Penn email, false otherwise.
     */
    private boolean pennEmail() {
        return emailString.endsWith(".upenn.edu");
    }

    /**
     * This function checks if a mobile number is valid.  We prompt the user to enter mobile
     * numbers without any punctuation, characters. or spaces.  The number must contain an area
     * code.
     * Valid input: 7328675309
     * Invalid inputs: 732-867-5309, (732) 867 5309, 8675309, password12
     * @param mobile
     * @return true if the mobile number is valid, false otherwise.
     */
    private boolean validMobile(String mobile) {
        if (mobile.length() != 10) {
            return false;
        }
        for (int i = 0; i < mobile.length(); i++) {
            try {
                Integer.parseInt(mobile.charAt(i) + "");
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * This function creates an account for the user if one does not exist already.  If a user
     * account does not exist for the email address, a new account is created and stored in the
     * Google Firestore database.  If a user account exists for the email address, a Toast is
     * displayed letting the user know.
     */
    private void createAccount() {
        Map<String, String> user = new HashMap<>();
        user.put("email", emailString);
        user.put("password", passwordString);
        user.put("mobile", mobileString);
        user.put("emergency_mobile", emergencyMobileString);
        user.put("first_name", firstNameString);
        user.put("last_name", lastNameString);

        /**
         * Check if a user account for this email address already exists.
         */
        DocumentReference docRef = db.collection("users").document(emailString);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                /**
                 * Case where account linked to emailString exists
                 */
                if (document.exists()) {
                    display("An account already exists with this email.", "Toast");
                } else {
                    db.collection("users").document(emailString)
                            .set(user)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

                    /**
                     * Send the current login information to the MapsActivity screen.
                     */
                    Intent mapsIntent = new Intent(this, MapsActivity.class);
                    mapsIntent.putExtra("Email", emailString);
                    startActivity(mapsIntent);
                }
            }
        });


    }

    /**
     * Displays the text field in either a Toast or AlertDialog (indicated by the type field).
     * @param text
     */
    private void display(String text, String type) {
        if (type.equals("Toast")) {
            Context context = getApplicationContext();
            CharSequence msg = text;
            int duration = Toast.LENGTH_SHORT;

            Toast t = Toast.makeText(context, msg, duration);
            t.show();
        } else if (type.equals("AlertDialog")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(text).setTitle("Error");
            builder.setNegativeButton("OK", (dialog, id) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}