/*
 Copyright (C) 2020  PVY Soft. All rights reserved.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 Contact info:
 PVY Soft
 email: pvysoft@gmail.com
*/

package com.faceit.faceitapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;

/**
 * This class represents activity that can update/set/check password.
 * Its purpose is defined by extras put in the Intent that starts it
 */
public class PasswordActivity extends AppCompatActivity {
    // init all elements of activity (edit texts and button)
    private EditText oldPasswordEditText;
    private EditText firstNewPassEditText;
    private EditText secondNewPassEditText;
    private Button passwordButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // Assign all elements of activity
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        firstNewPassEditText = findViewById(R.id.firstNewPassEditText);
        secondNewPassEditText = findViewById(R.id.secondNewPassEditText);
        passwordButton = findViewById(R.id.passwordButton);

        // Get information from extras about mode of app
        Intent inIntent = getIntent();
        String mode = inIntent.getStringExtra("mode");

        // There are three modes: update - when user wants to update
        // password, set - when user is required to set password (first openning)
        // check - when users enters app - it is needed to verify password
        switch (mode) {
            case "update":
                // show old password editText (it is hidden by default)
                oldPasswordEditText.setVisibility(View.VISIBLE);

                // OnClickListener for button
                passwordButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        // access data base + get text from edit texts
                        DataBase db = new DataBase(getApplicationContext());
                        String oldPassword = oldPasswordEditText.getText().toString();
                        String first = firstNewPassEditText.getText().toString();
                        String second = secondNewPassEditText.getText().toString();

                        try {
                            // if old password isn't correct
                            if (!db.checkPassword(oldPassword)) {
                                Toast.makeText(getApplicationContext(), "Wrong old password!",
                                        Toast.LENGTH_SHORT).show();
                                oldPasswordEditText.setText("");
                            // If passwords don't match
                            } else if (!first.equals(second)) {
                                Toast.makeText(getApplicationContext(), "Passwords don't match!",
                                        Toast.LENGTH_SHORT).show();
                            // else update password
                            } else {
                                db.setPassword(first);
                                Toast.makeText(getApplicationContext(),
                                        "Password updated successfully!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                                Intent startMainActivity = new Intent(getApplicationContext(), FaceRecognitionAppActivity.class);
                                startMainActivity.putExtra("start", "passwordChecked");
                                startActivity(startMainActivity);
                            }
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case "set":
                // change text on button
                passwordButton.setText("Set new password");
                passwordButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        // open db and get input
                        DataBase db = new DataBase(getApplicationContext());
                        String first = firstNewPassEditText.getText().toString();
                        String second = secondNewPassEditText.getText().toString();

                        // two passwords must match to set password
                        if (!first.equals(second)) {
                            Toast.makeText(getApplicationContext(), "Passwords don't match!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                db.setPassword(first);
                                Toast.makeText(getApplicationContext(),
                                        "Password was set successfully!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            case "check":
                // change hint and hide one of the editTexts
                firstNewPassEditText.setHint("Verify password");
                secondNewPassEditText.setVisibility(View.INVISIBLE);
                passwordButton.setText("Enter");

                passwordButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        DataBase db = new DataBase(getApplicationContext());
                        String password = firstNewPassEditText.getText().toString();

                        try {
                            if (!db.checkPassword(password)) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect password!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                finish();
                                Intent startMainActivity = new Intent(getApplicationContext(), FaceRecognitionAppActivity.class);
                                startMainActivity.putExtra("start", "passwordChecked");
                                startActivity(startMainActivity);
                            }
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }

                    }
                });

                break;
        }
    }

    /**
     * This is methods that is called when user presses back
     */
    @Override
    public void onBackPressed() {
        // if mode is set password or check password, user can't exit
        Intent inIntent = getIntent();
        String mode = inIntent.getStringExtra("mode");
        if (mode.equals("update")) {
            Intent startMainActivity = new Intent(getApplicationContext(), FaceRecognitionAppActivity.class);
            startMainActivity.putExtra("start", "passwordChecked");
            startActivity(startMainActivity);
            finish();
        }
        else if (mode.equals("set")){
            Toast.makeText(getApplicationContext(), "Please, set password",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
