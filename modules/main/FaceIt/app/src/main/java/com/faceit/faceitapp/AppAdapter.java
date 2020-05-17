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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * This class is adapter for recyclerView that shows all installed apps
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ItemViewHolder>{

    // init all containers that will be used, inflater for one holder and context
    LayoutInflater mInflater;
    private ArrayList<String> appsNames;
    private ArrayList<String> appsPackageNames;
    private ArrayList<String> appsLocked;
    private ArrayList<Drawable> appsIcons;
    private Context context;

    /**
     * Constructor. Its purpose - save given information.
     * @param apps - ArrayList of all names of apps
     * @param packages - ArrayList of all package names in apps
     * @param lockeds - ArrayList of package names of locked apps
     * @param icons - icons of all apps
     */
    public AppAdapter(ArrayList<String> apps, ArrayList<String> packages,
                      ArrayList<String> lockeds, ArrayList<Drawable> icons){
        appsNames = apps;
        appsPackageNames = packages;
        appsLocked = lockeds;
        appsIcons = icons;
    }

    /**
     * This method is called when new holder is created
     * @param parent - link to parent recyclerView
     * @param viewType - ???
     * @return ItemViewHolder inflated with details_layout.xml
     */
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        mInflater = LayoutInflater.from(context);

        View myView = mInflater.inflate(R.layout.details_layout, parent, false);

        return new ItemViewHolder(myView);

    }

    /**
     * Methods that binds holder (updates or just puts information)
     * @param holder - holder
     * @param position - position of holder in recyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // get app name. package name, lock status
        boolean locked = false;
        String name = appsNames.get(position);
        String package_name = appsPackageNames.get(position);
        if (!appsLocked.isEmpty())
            locked = appsLocked.contains(package_name);
        Drawable icon = appsIcons.get(position);

        // bind holder with given information
        holder.bind(name, package_name, locked, icon);
    }

    /**
     * @return length of recyclerView
     */
    @Override
    public int getItemCount() {
        return appsNames.size();
    }

    /**
     * class that represents one holder in recyclerView
     */
    class ItemViewHolder extends RecyclerView.ViewHolder{

        // Init all elements of holder
        TextView appNameTextView;
        ImageView appIconImageView;
        Switch lockedSwitch;

        /**
         * Constructor, just to assign elements of holder
         * @param itemView - inflated view, where all info will be binded
         */
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            // Assign those elements
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            appIconImageView = itemView.findViewById(R.id.appIconImageView);
            lockedSwitch = itemView.findViewById(R.id.lockedCheckBox);
        }

        /**
         * Binds holder with given information
         * @param appName - name of app
         * @param packageName - name of package
         * @param locked - status of app
         * @param icon - icon of app
         */
        void bind(String appName, final String packageName, boolean locked, Drawable icon){
            // Set app name, package name and status
            appNameTextView.setText(appName);
            appIconImageView.setImageDrawable(icon);
            lockedSwitch.setChecked(locked);

            // Set on click listener: update app status
            lockedSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lockedSwitch.isChecked()) {
                        appsLocked.add(packageName);
                        setLocked(true, packageName);
                        notifyDataSetChanged();
                    }
                    else {
                        setLocked(false, packageName);
                        appsLocked.remove(packageName);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    /**
     * Locks or unlocks application
     * @param locked - true/false
     * @param package_name - name of package
     */
    public void setLocked(boolean locked, String package_name){
        DataBase db = new DataBase(context);

        if (locked)
            db.setLocked(db.getChosenProfile(), package_name);
        else
            db.unsetLocked(db.getChosenProfile(), package_name);

        db.close();
    }
}
