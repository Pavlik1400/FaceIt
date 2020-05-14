package com.faceit.faceitapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;

public class PasswordActivity extends AppCompatActivity {
    private EditText oldPasswordEditText;
    private EditText firstNewPassEditText;
    private EditText secondNewPassEditText;
    private Button passwordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        firstNewPassEditText = findViewById(R.id.firstNewPassEditText);
        secondNewPassEditText = findViewById(R.id.secondNewPassEditText);
        passwordButton = findViewById(R.id.passwordButton);

        Intent inIntent = getIntent();
        String mode = inIntent.getStringExtra("mode");

        if (mode.equals("update")){
            oldPasswordEditText.setVisibility(View.VISIBLE);

            passwordButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    DataBase db = new DataBase(getApplicationContext());
                    String oldPassword = oldPasswordEditText.getText().toString();
                    String first = firstNewPassEditText.getText().toString();
                    String second = secondNewPassEditText.getText().toString();

                    try {
                        if (!db.checkPassword(oldPassword)){
                            Toast.makeText(getApplicationContext(), "Wrong old password!",
                                    Toast.LENGTH_SHORT).show();
                            oldPasswordEditText.setText("");
                        }
                        else if (!first.equals(second)){
                            Toast.makeText(getApplicationContext(), "Passwords don't match!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            db.setPassword(first);
                            Toast.makeText(getApplicationContext(),
                                    "Password updated successfully!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else if (mode.equals("set")){
            passwordButton.setText("Set new password");
            passwordButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    DataBase db = new DataBase(getApplicationContext());
                    String first = firstNewPassEditText.getText().toString();
                    String second = secondNewPassEditText.getText().toString();

                    if (!first.equals(second)){
                        Toast.makeText(getApplicationContext(), "Passwords don't match!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
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
        }
        else if (mode.equals("check")){
            firstNewPassEditText.setHint("Confirm password");
            secondNewPassEditText.setVisibility(View.INVISIBLE);
            passwordButton.setText("Enter");

            passwordButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    DataBase db = new DataBase(getApplicationContext());
                    String password = firstNewPassEditText.getText().toString();

                    try {
                        if (!db.checkPassword(password)){
                            Toast.makeText(getApplicationContext(),
                                    "Incorrect password!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            finish();
                        }
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        Intent inIntent = getIntent();
        String mode = inIntent.getStringExtra("mode");
        if (mode.equals("update"))
            super.onBackPressed();
        else if (mode.equals("set")){
            Toast.makeText(getApplicationContext(), "Please, set password",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
