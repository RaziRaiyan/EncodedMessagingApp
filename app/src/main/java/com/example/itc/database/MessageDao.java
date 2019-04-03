package com.example.itc.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    public void insertMessage(Message... messages);

    @Update
    public void update(Message... messages);

    @Delete
    public void delete(Message... messages);

    @Query("SELECT * FROM message WHERE id = :id")
    LiveData<Message> loadMessageById(int id);

    @Query("SELECT * FROM message")
    LiveData<List<Message>> loadAllMessage();
}
