package com.faceit.faceitapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ItemViewHolder>{

    LayoutInflater mInflater;
    private ArrayList<String> appsNames;
    private ArrayList<String> appsPackageNames;
    private ArrayList<String> appsLocked;
    private ArrayList<Drawable> appsIcons;
    private Context context;

    public AppAdapter(ArrayList<String> apps, ArrayList<String> packages,
                      ArrayList<String> lockeds, ArrayList<Drawable> icons){
        appsNames = apps;
        appsPackageNames = packages;
        appsLocked = lockeds;
        appsIcons = icons;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        mInflater = LayoutInflater.from(context);

        View myView = mInflater.inflate(R.layout.details_layout, parent, false);

        return new ItemViewHolder(myView);

    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        boolean locked = false;
        String name = appsNames.get(position);
        String package_name = appsPackageNames.get(position);
        if (!appsLocked.isEmpty())
            locked = appsLocked.contains(package_name);
        Drawable icon = appsIcons.get(position);

        holder.bind(name, package_name, locked, icon);
    }

    @Override
    public int getItemCount() {
        return appsNames.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView appNameTextView;
        ImageView appIconImageView;
        CheckBox lockedCheckBox;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            appIconImageView = itemView.findViewById(R.id.appIconImageView);
            lockedCheckBox = itemView.findViewById(R.id.lockedCheckBox);
        }

        void bind(String appName, final String packageName, boolean locked, Drawable icon){
            appNameTextView.setText(appName);
            appIconImageView.setImageDrawable(icon);
            lockedCheckBox.setChecked(locked);

            lockedCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lockedCheckBox.isChecked())
                        setLocked(true, packageName);
                    else
                        setLocked(false, packageName);
                }
            });
        }
    }
    public void setLocked(boolean locked, String package_name){
        DataBase db = new DataBase(context);

        if (locked)
            db.setLocked(db.getChosenProfile(), package_name);
        else
            db.unsetLocked(db.getChosenProfile(), package_name);

        db.close();
    }
}
