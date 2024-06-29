package com.example.snapdiaryv3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReminderService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Handle service logic here if needed
        return super.onStartCommand(intent, flags, startId);
    }
}
