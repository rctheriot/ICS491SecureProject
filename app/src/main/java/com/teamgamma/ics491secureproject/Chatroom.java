package com.teamgamma.ics491secureproject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Chatroom Activity
 * This is the activity the user views when he joins a specific chatroom.
 */
public class Chatroom extends AppCompatActivity {

    private EditText mMessageField;
    private Button mSendMessage;

    private String username;
    private String roomid;

    private ArrayList<String> mChats = new ArrayList<>();
    private ArrayList<String> mKeys = new ArrayList<>();
    private ListView mChatsList;
    private ArrayAdapter<String> chatroomsAdapter;

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        //Get Firebase Authentication Instance
        mAuth = FirebaseAuth.getInstance();

        //Firebase Authentication State Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Get the user currently signed in
                user = firebaseAuth.getCurrentUser();

                //If the user is not null then show the page, else change to Login Activity
                if (user != null) {
                    //Get the roomid the user has joined from the Intent message
                    Intent intent = getIntent();
                    roomid = intent.getStringExtra(DisplayChatrooms.ROOM_MESSAGE);

                    //Populate Activity's text fields
                    mMessageField = (EditText) findViewById(R.id.messageText);
                    setChatTitle();
                    setUsername();
                    displayChat();

                    //Thread policy for Android so we can do https request
                    //Required to call the Server Functions
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                } else {
                    //Change to Login Page
                    changeToLogin();
                }
            }
        };

        //Bind Send button to sendMessage Function
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

    /**
     * Change to Login page
     */
    private void changeToLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    /**
     * Populates the chatroom with the messages in the database
     */
    private void displayChat() {

        //Get the Chatroom's reference from Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomid).child("messages").getRef();

        //Get the Android ListView within the Activity
        mChatsList = (ListView) findViewById(R.id.chatList);

        //Array Adapter (Links an Android ListView to an Array)
        chatroomsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChats);
        mChatsList.setAdapter(chatroomsAdapter);

        //Firebase Event Listeners
        mDatabase.addChildEventListener(new ChildEventListener() {

            //If a new message is added to database update list
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String username = dataSnapshot.child("username").getValue(String.class);
                String message = dataSnapshot.child("message").getValue(String.class);
                mChats.add(username + "  -  " + message);
                String key = dataSnapshot.getKey();
                mKeys.add(key);
                chatroomsAdapter.notifyDataSetChanged();
            }

            //If a message is changed in database update list
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

    /**
     * Send a message to database
     */
    private void sendMessage() {

        //Bind message from Message Text Field
        String message = mMessageField.getText().toString();

        //Encode message with the URL Encoder to ensure no funny business for XSS
        try {
            message = URLEncoder.encode(message, "UTF-8");
        } catch (IOException e) {
            return;
        }

        //Create the https url string to call server function
        String urlString =
                "https://us-central1-ics491-3e72d.cloudfunctions.net/addMessage?" +
                        "u=" + user.getUid().toString() +
                        "&r=" + roomid +
                        "&m=" + message;

        //Try sending message using Java's HTTP URL libs
        try {
            URL myURL = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();
            urlConnection.getInputStream();
            urlConnection.disconnect();
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Set the title of the chatroom by obtaining name from database
     */
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

    /**
     * Set username by referencing database
     */
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
