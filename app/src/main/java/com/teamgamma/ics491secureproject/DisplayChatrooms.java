package com.teamgamma.ics491secureproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DisplayChatrooms extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ArrayList<String> mChatrooms = new ArrayList<>();
    private ArrayList<String> mKeys = new ArrayList<>();
    private ListView mChatroomList;
    private ArrayAdapter<String> chatroomsAdapter;
    protected static final String ROOM_MESSAGE = "roomid";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_chatrooms);
        setTitle("Chatrooms");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Email_Password", "onAuthStateChanged:signed_in:" + user.getUid());
                    displayRooms();

                } else {
                    // User is signed out
                    Log.d("Email_Password", "onAuthStateChanged:signed_out");
                    changeToLogin();
                }
                // ...
            }
        };
    }

    private void displayRooms() {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chatrooms").getRef();
        mChatroomList = (ListView) findViewById(R.id.chatroomList);
        chatroomsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChatrooms);
        mChatroomList.setAdapter(chatroomsAdapter);

        mChatroomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), Chatroom.class);
                intent.putExtra(ROOM_MESSAGE, mKeys.get(position));
                startActivity(intent);
            }
        });

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

    private void changeToLogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
