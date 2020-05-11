package com.example.applocker4;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewItemAdapter extends RecyclerView.Adapter<NewItemAdapter.ItemViewHolder>{

    LayoutInflater mInflater;
    private List<String> appsNames;
    private List<String> appsPackageNames;
    private List<String> appsLocked;
    private List<Drawable> appsIcons;
    private Context context;

    public NewItemAdapter(List<String> apps, List<String> packages,
                          List<String> lockeds, List<Drawable> icons){
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

        ItemViewHolder viewHolder = new ItemViewHolder(myView);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String name = appsNames.get(position);
        String package_name = appsPackageNames.get(position);
        String locked = appsLocked.get(position);
        Drawable icon = appsIcons.get(position);

        holder.bind(name, locked, package_name, icon);
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

        void bind(String appName, String locked, final String packageName, Drawable icon){
            appNameTextView.setText(appName);
            appIconImageView.setImageDrawable(icon);
            if (locked.equals("true"))
                lockedCheckBox.setChecked(true);
            else
                lockedCheckBox.setChecked(false);

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
        DataBase dbHelper = new DataBase(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues new_val = new ContentValues();

        if (locked)
            new_val.put(DataBase.FeedEntry.COLUMN_NAME_LOCKED, "true");
        else
            new_val.put(DataBase.FeedEntry.COLUMN_NAME_LOCKED, "false");

        db.update(DataBase.FeedEntry.TABLE_NAME, new_val,
                DataBase.FeedEntry.COLUMN_NAME_PACKAGE_NAME + " = ?",
                new String[] {package_name});

        db.close();
    }
}
