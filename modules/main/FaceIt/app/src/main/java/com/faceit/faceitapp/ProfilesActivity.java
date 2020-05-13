package com.faceit.faceitapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ProfilesActivity extends AppCompatActivity {
    private RecyclerView profilesRecyclerView;
    private FloatingActionButton addProfileFloatingActionButton;
    private EditText profileNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        addProfileFloatingActionButton = findViewById(R.id.addProfileFloatingActionButton);
        profileNameEditText = findViewById(R.id.profileNameEditText);
        profilesRecyclerView = findViewById(R.id.profilesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        profilesRecyclerView.setLayoutManager(layoutManager);

        final DataBase db = new DataBase(getApplicationContext());
        final ArrayList<String> allProfiles = db.getAllProfiles();

        final ProfileItemAdapter adapter = new ProfileItemAdapter(allProfiles);

        profilesRecyclerView.setAdapter(adapter);

        addProfileFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profileName = profileNameEditText.getText().toString();
                if (!profileName.equals("") && !allProfiles.contains(profileName)){
                    db.createNewProfile(profileNameEditText.getText().toString(), "false");
                    allProfiles.add(profileName);
                    profilesRecyclerView.getAdapter().notifyDataSetChanged();
                    profilesRecyclerView.smoothScrollToPosition(allProfiles.size()-1);

                    profileNameEditText.setText("");
                }
            }
        });

        db.close();

    }
}
