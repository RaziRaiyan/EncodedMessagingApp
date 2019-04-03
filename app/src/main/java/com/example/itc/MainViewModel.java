package com.example.itc;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.itc.database.AppDatabase;
import com.example.itc.database.Message;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<Message>> messages;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Activily retrieving the message list from the database");
        messages = appDatabase.messageDao().loadAllMessage();
    }

    public LiveData<List<Message>> getFriends(){
        return messages;
    }
}
