package com.teamgamma.ics491secureproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Chatroom extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ArrayList<String> mChats = new ArrayList<>();
    private ArrayList<String> mKeys = new ArrayList<>();
    private ListView mChatsList;
    private ArrayAdapter<String> chatroomsAdapter;

    private String username;
    private String roomid;

    private EditText mMessageField;

    private Button mSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Email_Password", "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = getIntent();
                    roomid = intent.getStringExtra(DisplayChatrooms.ROOM_MESSAGE);
                    mMessageField = (EditText) findViewById(R.id.messageText);
                    setChatTitle();
                    setUsername();
                    displayChat();

                } else {
                    // User is signed out
                    Log.d("Email_Password", "onAuthStateChanged:signed_out");
                    changeToLogin();
                }
                // ...
            }
        };

        mSendMessage = (Button) findViewById(R.id.sendmessageBtn);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void displayChat() {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomid).child("messages").getRef();
        mChatsList = (ListView) findViewById(R.id.chatList);
        chatroomsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChats);
        mChatsList.setAdapter(chatroomsAdapter);

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String username = dataSnapshot.child("username").getValue(String.class);
                String message = dataSnapshot.child("message").getValue(String.class);
                mChats.add(username + "  -  " + message);
                String key = dataSnapshot.getKey();
                mKeys.add(key);
                chatroomsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String username = dataSnapshot.child("username").getValue(String.class);
                String message = dataSnapshot.child("message").getValue(String.class);
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);
                mChats.set(index, username + "  -  " + message);
                chatroomsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class Message {
        public String username;
        public String message;
        public long time;

        public Message(String a, String m, long ts) {
            username = a;
            message = m;
            time = ts;
        }
    }

    private void sendMessage () {

        String message = mMessageField.getText().toString();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        long timeStamp = ts.getTime();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomid).getRef();
        mDatabase.child("messages").push().setValue(new Message(username, message, timeStamp)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                } else {
                    Toast.makeText(Chatroom.this, "Message Failed to Send", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void changeToLogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    private void setChatTitle() {
        DatabaseReference mRoomTitle = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomid).child("name").getRef();
        mRoomTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomtitle = dataSnapshot.getValue(String.class);
                setTitle(roomtitle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUsername() {
        DatabaseReference mUser = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("username").getRef();
        mUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
