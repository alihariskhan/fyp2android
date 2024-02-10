package com.secguard.securityguardmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class login_activity extends AppCompatActivity {

    private EditText editTextGuardId, editTextPassword;
    private String loggedInGuardId;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in
        sessionManager = new SessionManager(this);
        if (sessionManager.getGuardId() != null) {
            startMainActivity();
            return;
        }
        setContentView(R.layout.activity_login);

        editTextGuardId = findViewById(R.id.editTextGuardId);
        editTextPassword = findViewById(R.id.editTextPassword);

        CheckBox checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Toggle password visibility based on checkbox state
                int inputType = isChecked ? android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
                editTextPassword.setInputType(inputType);
                editTextPassword.setSelection(editTextPassword.getText().length()); // Keep cursor at the end
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guardId = editTextGuardId.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Execute AsyncTask to perform login
                new LoginTask().execute(guardId, password);
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(login_activity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String guardId = params[0];
            String password = params[1];

            try {
                // Set up the connection
                URL url = new URL("http://192.168.76.199/SecurityGuardManagement/login.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Send login credentials
                JSONObject postData = new JSONObject();
                postData.put("guard_id", guardId);
                postData.put("password", password);

                OutputStream os = urlConnection.getOutputStream();
                os.write(postData.toString().getBytes("UTF-8"));
                os.close();

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Disconnect the connection
                urlConnection.disconnect();

                return response.toString();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return "Error occurred";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            handleLoginResult(result);
        }
    }

    @Override
    public void onBackPressed() {
        // Override the back button behavior if you don't want to go back to MainActivity after login
        moveTaskToBack(true);
    }

    private void handleLoginResult(String result) {
        // Handle the result from the server
        if (result.equals("Login successful")) {
            // Save user info in SharedPreferences
            sessionManager.setGuardId(editTextGuardId.getText().toString().trim());

            // Start the main activity
            startMainActivity();

            // Start the main activity or perform other actions
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(login_activity.this, MainActivity.class);
             startActivity(intent);
             finish();
        } else if (result.equals("Invalid credentials")) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }
    }
}
