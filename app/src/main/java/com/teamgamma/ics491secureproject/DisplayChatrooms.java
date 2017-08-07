package com.teamgamma.ics491secureproject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Chatroom List Activity
 * This is the activity the user views when he joins a specific chatroom.
 */
public class DisplayChatrooms extends AppCompatActivity {

    private ArrayList<String> mChatrooms = new ArrayList<>();
    private ArrayList<String> mKeys = new ArrayList<>();
    private ListView mChatroomList;
    private ArrayAdapter<String> chatroomsAdapter;

    protected static final String ROOM_MESSAGE = "roomid";

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_chatrooms);

        //Set title of Page
        setTitle("Chatrooms");

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
                    //Display the chatroom list
                    displayRooms();

                    //Thread policy for Android so we can do https request
                    //Required to call the Server Functions
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                } else {
                    // User is signed out
                    Log.d("Email_Password", "onAuthStateChanged:signed_out");
                    changeToLogin();
                }
            }
        };
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
    private void changeToLogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    /**
     * Display the chatroom list
     */
    private void displayRooms() {

        //Get the chatroom list reference in the database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("chatrooms").getRef();

        //Set up the List with an Android ArrayAdapter
        mChatroomList = (ListView) findViewById(R.id.chatroomList);
        chatroomsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChatrooms);
        mChatroomList.setAdapter(chatroomsAdapter);

        //For each item in the list setup a Click Listener
        mChatroomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Position in list
                final int pos = position;
                final View v = view;

                //Create the Password Alert Dialog for user to input Chatroom Password
                final EditText passwordInput = new EditText(DisplayChatrooms.this);
                passwordInput.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                AlertDialog.Builder alertPassword = new AlertDialog.Builder (DisplayChatrooms.this);
                alertPassword.setTitle("Enter Password");
                alertPassword.setView(passwordInput);

                //Create a button on Listener for user to submit password
                alertPassword.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //The entered password
                        String password = passwordInput.getText().toString();

                        //Encode message with the URL Encoder to ensure no funny business for XSS
                        try {
                            password = URLEncoder.encode(password, "UTF-8");
                        } catch (IOException e) {
                            return;
                        }

                        //Create the https url string to call server function
                        String urlString =
                                "https://us-central1-ics491-3e72d.cloudfunctions.net/roomPasswordCheck?" +
                                        "r=" +  mKeys.get(pos) +
                                        "&p=" + password;

                        //Try sending message using Java's HTTP URL libs
                        try {
                            URL myURL = new URL(urlString);
                            HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();
                            int code = urlConnection.getResponseCode();

                            //If Response code is 200 take user to chatroom. Else display message
                            if (code == 200) {
                                Intent intent = new Intent(v.getContext(), Chatroom.class);
                                intent.putExtra(ROOM_MESSAGE, mKeys.get(pos));
                                startActivity(intent);
                            }
                            else {
                                dialog.cancel();
                                Toast.makeText(DisplayChatrooms.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                            }

                            //Disconnect URL Connection
                            urlConnection.disconnect();
                        } catch (IOException e) {
                            return;
                        }

                    }
                });

                //Setup a Cancel button on Dialog box
                alertPassword.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                //Show the Password dialog
                alertPassword.show();

            }
        });

        //Database listeners. If new chatrooms are created update the list
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String creator = dataSnapshot.child("username").getValue(String.class);
                mChatrooms.add(name + "  -  " + creator);
                String key = dataSnapshot.getKey();
                mKeys.add(key);
                chatroomsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String creator = dataSnapshot.child("username").getValue(String.class);
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);
                mChatrooms.set(index, name + "  -  " + creator);
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


}
