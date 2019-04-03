package com.example.itc.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;

import com.google.firebase.database.Exclude;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "message")
public class Message {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String message;
    private String senderUid;
    private String encodedMessage;

    @Ignore
    public Message(){}

    @Ignore
    public Message(String message, String senderUid,String encodedMessage) {
        this.message = message;
        this.senderUid = senderUid;
        this.encodedMessage = encodedMessage;
    }

    public Message(int id, String message, String senderUid,String encodedMessage) {
        this.id = id;
        this.message = message;
        this.senderUid = senderUid;
        this.encodedMessage = encodedMessage;
    }

    public String getEncodedMessage() {
        return encodedMessage;
    }

    public void setEncodedMessage(String encodedMessage) {
        this.encodedMessage = encodedMessage;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
