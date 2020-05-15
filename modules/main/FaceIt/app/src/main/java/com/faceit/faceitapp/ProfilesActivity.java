/**
 * This class represents activity with profiles. main element of its layout - recyclerView
 * that uses ProfileItemAdapter as adapter.
 */
package com.faceit.faceitapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ProfilesActivity extends AppCompatActivity {
    // Init recyclerView, addButton and editText for typing name of new profile
    private RecyclerView profilesRecyclerView;
    private FloatingActionButton addProfileFloatingActionButton;
    private EditText profileNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        // assign elements of layout
        addProfileFloatingActionButton = findViewById(R.id.addProfileFloatingActionButton);
        profileNameEditText = findViewById(R.id.profileNameEditText);

        // Init recyclerView and set linear layout
        profilesRecyclerView = findViewById(R.id.profilesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        profilesRecyclerView.setLayoutManager(layoutManager);

        // Get list of all profiles from database
        final DataBase db = new DataBase(getApplicationContext());
        final ArrayList<String> allProfiles = db.getAllProfiles();

        // set adapter
        final ProfileItemAdapter adapter = new ProfileItemAdapter(allProfiles);
        profilesRecyclerView.setAdapter(adapter);

        // Bind add button
        addProfileFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get name of profile
                String profileName = profileNameEditText.getText().toString();
                if (!profileName.equals("") && !allProfiles.contains(profileName)){
                    // add profile to db
                    db.createNewProfile(profileNameEditText.getText().toString(), "false");
                    allProfiles.add(profileName);
                    // update recycleView
                    profilesRecyclerView.getAdapter().notifyDataSetChanged();
                    profilesRecyclerView.smoothScrollToPosition(allProfiles.size()-1);

                    profileNameEditText.setText("");
                } else if (profileName.equals("")){
                    Toast.makeText(getApplicationContext(), "Name can't be empty!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        db.close();

    }
}
