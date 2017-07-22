package com.teamgamma.ics491secureproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateChatroom extends AppCompatActivity {

    private Button mCreateRoomButton;
    private EditText mRoomNameField;
    private EditText mRoomPasswordField;
    private String username;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chatroom);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("EmailPassword", "onAuthStateChanged:signed_in:" + user.getUid());
                    DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("username").getRef();
                    userDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            username = dataSnapshot.getValue(String.class);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // User is signed out
                    Log.d("EmailPassword", "onAuthStateChanged:signed_out");
                    changeToLogin();
                }
                // ...
            }
        };

        mRoomNameField = (EditText) findViewById(R.id.chatroomnameText);
        mRoomPasswordField = (EditText) findViewById(R.id.roompasswordText);

        mCreateRoomButton = (Button) findViewById(R.id.createChatroomBtn);

        mCreateRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoom();
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

    public static class Chatroom {
        public String username;
        public String name;
        public String password;

        public Chatroom(String a, String m, String p) {
            username = a;
            name = m;
            password = p;
        }
    }

    private void createRoom() {

        String name = mRoomNameField.getText().toString();
        String password = mRoomPasswordField.getText().toString();

        //Check if room name is longer than 4 characters
        if (name.length() < 4) {
            Toast.makeText(CreateChatroom.this, "Room names require 4 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Check if room name is longer than 4 characters
        if (password.length() < 6) {
            Toast.makeText(CreateChatroom.this, "Password require 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference chatrooms = FirebaseDatabase.getInstance().getReference().child("chatrooms").getRef();
        chatrooms.push().setValue(new Chatroom(username, name, password)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreateChatroom.this, "Chatroom Created", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateChatroom.this, "Failed to Create", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
