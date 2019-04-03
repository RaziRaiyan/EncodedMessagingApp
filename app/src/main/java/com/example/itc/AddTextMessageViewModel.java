package com.example.itc;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.itc.database.AppDatabase;
import com.example.itc.database.Message;

public class AddTextMessageViewModel extends ViewModel {

    LiveData<Message> friend;

    public AddTextMessageViewModel(AppDatabase database, int id) {
        this.friend = database.messageDao().loadMessageById(id);
    }

    public LiveData<Message> getFriend(){
        return friend;
    }
}
