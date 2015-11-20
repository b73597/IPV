package com.farr.android.farrapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class MainActivity extends Activity {

    //Variables
    Button signUp;
    Button logIn;
    Button resetPass;

    String userName;
    String userPassword;
    //String passReset;


    EditText user;
    EditText pass;
    //EditText reset;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parse.initialize(this, "qf3R1DBaQgWRrlc15HyFPy5W6GPutAjN7nJQ0Me5", "NmldmaNb3wZJsW58xp8iEydADxO4BLLna25yJRXM");

        setContentView(R.layout.activity_main);

        user = (EditText) findViewById(R.id.editText);
        pass = (EditText) findViewById(R.id.editText2);



        signUp = (Button) findViewById(R.id.button);
        logIn = (Button) findViewById(R.id.button2);
        resetPass = (Button) findViewById(R.id.button3);


        // Button onClick Listener
        signUp.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                userName = user.getText().toString();
                userPassword = pass.getText().toString();

                //send to parse
                ParseUser user = new ParseUser();
                user.setUsername(userName);
                user.setPassword(userPassword);
                user.signUpInBackground(new SignUpCallback() {

                    public void done(ParseException e) {
                        if (e == null) {
                            // Successful Sign Up sends user to Map Activity activity
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    MapActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });



                // Login Button
                logIn.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {
                        // Get user input from EditText fields
                        userName = user.getText().toString();
                        userPassword = pass.getText().toString();

                        // Credentials verification
                        ParseUser.logInInBackground(userName, userPassword,
                                new LogInCallback() {
                                    public void done(ParseUser user, ParseException e) {
                                        if (user != null) {

                                            // Successful Login sends user to CRUD activity
                                            Intent intent = new Intent(
                                                    MainActivity.this,
                                                    MapActivity.class);
                                            startActivity(intent);

                                            Toast.makeText(getApplicationContext(),
                                                    "Successful Log in",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "No user found. Please create account",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                    }
                });
        // reset Button
        resetPass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(
                        MainActivity.this,
                        ResetPasswordActivity.class);
                startActivity(intent);
                }
            });
        }
    }