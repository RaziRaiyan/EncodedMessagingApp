package com.example.itc;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.itc.database.AppDatabase;
import com.example.itc.database.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

public class TextMessageFragment extends Fragment
        implements RecyclerItemClickListener.OnRecyclerClickListner{
    private static final String TAG = TextMessageFragment.class.getSimpleName();

    RecyclerView myRecyclerView;
    TextMessageAdapter mTextMessageAdapter;
    LinearLayoutManager mLinearLayoutManager;

    private static int totalTextMessageCount = 0;
    private static final String MESSAGE_PREFERENCE = "MESSAGE_PREFERENCE";
    private static final String MESSAGE_COUNT = "MESSAGE_COUNT";

    private static final String CLOUD_TEXT_MESSAGE = "text_messages";

    private static boolean flag = true;

    SharedPreferences mSharedPreferences ;
    SharedPreferences.Editor mEditor;

    private EditText etMessage;
    private FloatingActionButton fab;

    private AppDatabase mAppDatabase;

    private static List<Message> mMessageList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSharedPreferences = this.getActivity().getSharedPreferences(MESSAGE_PREFERENCE,0);
        totalTextMessageCount = mSharedPreferences.getInt(MESSAGE_COUNT,0);
        mEditor = mSharedPreferences.edit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text,container,false);
        myRecyclerView = view.findViewById(R.id.text_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(mLinearLayoutManager);
        myRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),0));
        myRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(),myRecyclerView,this));

        Log.d(TAG, "onCreateView: Adding Adapter");
        mTextMessageAdapter = new TextMessageAdapter(getContext());
        myRecyclerView.setAdapter(mTextMessageAdapter);
        mAppDatabase = AppDatabase.getInstance(getContext());

        etMessage = view.findViewById(R.id.et_text);
        fab = view.findViewById(R.id.fab_text);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });
        setupViewModel();
        setUpMessageCounter();
        setupReceiver();
        return view;
    }

    private void setUpMessageCounter(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(MESSAGE_COUNT);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    totalTextMessageCount = Integer.parseInt(dataSnapshot.getValue().toString());
                }catch (Exception e){
                    Log.d(TAG, "line 107 onDataChange: "+e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dbRef.setValue(totalTextMessageCount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "line 120 onComplete: text message counter successfully added");
                }else {
                    Log.d(TAG, "line 122 onComplete: problem adding text message counter");
                }
            }
        });
    }
    
    private void setupReceiver() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbRefMessageCount = dbRef.child(MESSAGE_COUNT);
        final int tempCount = totalTextMessageCount;
        dbRefMessageCount.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try{
                            totalTextMessageCount = Integer.parseInt(dataSnapshot.getValue().toString());
                        }catch (Exception e){
                            Log.d(TAG, "line 137 onDataChange: "+e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        final DatabaseReference dbRefTextMessage = dbRef.child(CLOUD_TEXT_MESSAGE);
        dbRefTextMessage.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try{

                    String senderUid = dataSnapshot.child("senderUid").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    if(!dataSnapshot.getKey().equals("message"+(totalTextMessageCount-1)) && senderUid==App.getThisUserUid()                   ){
                        return;
                    }
                    String encodedMessage = dataSnapshot.child("encodedMessage").getValue().toString();
                    String decodedMessage = LZWdecoder.decode(encodedMessage);
                    final Message receivedMessage = new Message(decodedMessage,senderUid,encodedMessage);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mAppDatabase.messageDao().insertMessage(receivedMessage);
                        }
                    });
                }catch (Exception e){
                    Log.d(TAG, "line 164 onChildAdded: "+e.getMessage());
                    Log.d(TAG, "line 165 onChildAdded: "+dataSnapshot.getValue());
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendTextMessage() {
        String text = etMessage.getText().toString();
        etMessage.getText().clear();
        if(text.trim().equals("")){
            etMessage.setError("Please write something to send text");
            etMessage.requestFocus();
            return;
        }

        if(App.getThisUserUid()==null){
            Toast.makeText(getActivity(),"You are not properly logged in, " +
                    "try logout and login to send message",Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d(TAG, "line 208 sendTextMessage: total text message count = "+totalTextMessageCount);
        String encodedString = LZWencoder.encode(text);
        final Message textMessage = new Message(text,App.getThisUserUid(),encodedString);
        totalTextMessageCount++;
        mEditor.putInt(MESSAGE_COUNT,totalTextMessageCount);
        mEditor.commit();
        setUpMessageCounter();
        if(addTextMessageToCloud(textMessage)){
            Log.d(TAG, "line 215 sendTextMessage: message added to cloud");
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mAppDatabase.messageDao().insertMessage(textMessage);
                    Log.d(TAG, "line 224 sendTextMessage: message added to database");
                }
            });
        }else {
            Log.d(TAG, "line 228 sendTextMessage: problem adding message to cloud");
        }
    }

    private boolean addTextMessageToCloud(Message textMessage) {
        Log.d(TAG, "line 233 addTextMessageToCloud: ");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(CLOUD_TEXT_MESSAGE);

        dbRef.child("message"+(totalTextMessageCount)).setValue(textMessage)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "line 241 onComplete: text message successfully added to cloud");
                            flag =true;
                        }else {
                            Log.d(TAG, "line 244 onComplete: problem adding text message");
                            flag = false;

                        }
                    }
                });
        return flag;
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFriends().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                Log.d(TAG, "line 258 List of messages updated from LiveData in ViewModel");
                for(Message message:messages){
                    Log.d(TAG, "line 260 found"+message.toString());
                }
                mMessageList = messages;
                mTextMessageAdapter.setMessages(messages);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {

        Toast.makeText(getActivity(),mMessageList.get(position).getSenderUid(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
