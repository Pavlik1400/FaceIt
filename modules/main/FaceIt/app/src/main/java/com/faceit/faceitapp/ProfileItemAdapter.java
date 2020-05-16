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

/**
 * This class is adapter for recyclerView in profiles menu
 */
public class ProfileItemAdapter extends RecyclerView.Adapter<ProfileItemAdapter.ProfileItemViewHolder> {
    // Init inflater, list for names and context
    LayoutInflater mInflater;
    private ArrayList<String> profilesNames;
    private Context context;

    /**
     * Constructor that just saves profiles names
     * @param profiles - names of profiles
     */
    public ProfileItemAdapter(ArrayList<String> profiles){
        profilesNames = profiles;
    }

    /**
     * This method is called when holder is created
     * @param parent - link to parent RecyclerView
     * @param viewType - ???
     * @return ProfileItemAdapter inflated with profile_layout.xml
     */
    @NonNull
    @Override
    public ProfileItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        mInflater = LayoutInflater.from(context);

        View myView = mInflater.inflate(R.layout.profile_layout, parent, false);

        return new ProfileItemViewHolder(myView);
    }

    /**
     * Methods that binds holder (updates or just puts information)
     * @param holder - holder
     * @param position - position of holder in recyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileItemViewHolder holder, int position) {
        String profileName = profilesNames.get(position);
        holder.bind(profileName, position);
    }

    /**
     * @return RecyclerView length
     */
    @Override
    public int getItemCount() {
        return profilesNames.size();
    }

    /**
     * Class thar represents one holder
     */
    class ProfileItemViewHolder extends RecyclerView.ViewHolder{
        // Init elements of layout
        private TextView profileNameTextView;
        private CheckBox chooseProfileCheckBox;
        private Button deleteButton;


        /**
         * Just constructor that assigns all Views
         * @param itemView - inflated view, where all info will be binded
         */
        public ProfileItemViewHolder(@NonNull View itemView) {
            super(itemView);
            profileNameTextView = itemView.findViewById(R.id.profileNameTextView);
            chooseProfileCheckBox = itemView.findViewById(R.id.chooseProfileCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        /**
         * Methods that bind holder Views with given information
         * @param profileName - name of profile
         * @param position - position of holder
         */
        void bind(final String profileName, final int position){
            // Access to database
            final DataBase db = new DataBase(context);
            profileNameTextView.setText(profileName);

            // set checkbox if profile is chosen
            chooseProfileCheckBox.setChecked(db.getChosenProfile().equals(profileName));

            // Bind checkbox
            chooseProfileCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!db.getChosenProfile().equals(profileName)){
                        db.setChosenProfile(profileName);
                    }
                    notifyDataSetChanged();
                }
            });

            // Bind delete button
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Impossible to delete chosen profile
                    if (!db.getChosenProfile().equals(profileName)) {
                        db.deleteProfile(profileName);
                        profilesNames.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, profilesNames.size());
                    }
                }
            });
            db.close();
        }
    }
}
