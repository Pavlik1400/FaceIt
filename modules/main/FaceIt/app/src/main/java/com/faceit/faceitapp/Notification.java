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

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

public class Notification extends Application {
    //*Class for creating notification */
    public static final String CHANNEL_ID = "12121212";

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Blocking apps",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("description");
            //serviceChannel.setLightColor(Color.BLUE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null){
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}

