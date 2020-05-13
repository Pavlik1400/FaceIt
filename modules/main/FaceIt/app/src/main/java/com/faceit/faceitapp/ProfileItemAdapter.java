package com.faceit.faceitapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileItemAdapter extends RecyclerView.Adapter<ProfileItemAdapter.ProfileItemViewHolder> {
    LayoutInflater mInflater;
    private ArrayList<String> profilesNames;
    private Context context;

    public ProfileItemAdapter(ArrayList<String> profiles){
        profilesNames = profiles;

    }

    @NonNull
    @Override
    public ProfileItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        mInflater = LayoutInflater.from(context);

        View myView = mInflater.inflate(R.layout.profile_layout, parent, false);

        return new ProfileItemViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileItemViewHolder holder, int position) {
        String profileName = profilesNames.get(position);
        holder.bind(profileName, position);
    }

    @Override
    public int getItemCount() {
        return profilesNames.size();
    }

    public void append(String profileName){

    }

    class ProfileItemViewHolder extends RecyclerView.ViewHolder{
        private TextView profileNameTextView;
        private CheckBox chooseProfileCheckBox;
        private Button deleteButton;


        public ProfileItemViewHolder(@NonNull View itemView) {
            super(itemView);

            profileNameTextView = itemView.findViewById(R.id.profileNameTextView);
            chooseProfileCheckBox = itemView.findViewById(R.id.chooseProfileCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final String profileName, final int position){
            final DataBase2 db = new DataBase2(context);
            profileNameTextView.setText(profileName);

            chooseProfileCheckBox.setChecked(db.getChosenProfile().equals(profileName));

            chooseProfileCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!db.getChosenProfile().equals(profileName)){
                        db.setChosenProfile(profileName);
                    }
                    notifyDataSetChanged();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!db.getChosenProfile().equals(profileName)) {
                        db.deleteProfile(profileName);
                        if (!db.hasProfile()) {
                            db.createNewProfile("Default", "true");
                            notifyDataSetChanged();
                        } else {
                            profilesNames.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, profilesNames.size());
                        }
                    }
                }
            });
            db.close();
        }
    }
}
