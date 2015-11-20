package com.farr.android.farrapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class ResetPasswordActivity extends AppCompatActivity {

    Button resetPassword;
    String passReset;
    EditText reset;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset);

        reset = (EditText) findViewById(R.id.editText4);
        resetPassword = (Button) findViewById(R.id.button4);


        // reset Button
        resetPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                passReset = reset.getText().toString();

                ParseUser.requestPasswordResetInBackground("test@gmail.com",
                        new RequestPasswordResetCallback() {
                            public void done(ParseException e) {


                                if (e == null) {
                                    // email successfully sent with reset
                                    Toast.makeText(getApplicationContext(), "Email Sent", Toast.LENGTH_LONG).show();
                                } else {
                                    // Something went wrong
                                    Toast.makeText(getApplicationContext(), "Wrong", Toast.LENGTH_LONG).show();

                                }
                                ;
                            }

                        });
            }
        });
    }
}
