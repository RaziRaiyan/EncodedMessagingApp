package com.example.itc;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class App extends Application {
    private static final String TAG = "App";
    public static String thisUserUid;

    public static String getThisUserUid() {
        return thisUserUid;
    }

    public static void setThisUserUid(String thisUserUid) {
        Log.d(TAG, "setThisUserUid: uid: "+thisUserUid);
        App.thisUserUid = thisUserUid;
    }
}
