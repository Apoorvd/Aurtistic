package com.blogspot.atifsoftwares.firebaseapp;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
