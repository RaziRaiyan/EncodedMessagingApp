package com.example.itc;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.itc.database.AppDatabase;

public class AddTextMessageViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private AppDatabase mDb;
    private int id;

    public AddTextMessageViewModelFactory(AppDatabase db, int friendId) {
        mDb = db;
        id = friendId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddTextMessageViewModel(mDb,id);
    }
}
