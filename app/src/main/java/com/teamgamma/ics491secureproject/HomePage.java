package com.teamgamma.ics491secureproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class HomePage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private Button mSignOutButton;
    private Button mViewChatroomsButton;
    private Button mCreateChatroomButton;

    private TextView mUsernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setTitle("Homepage");

        mUsernameText = (TextView) findViewById(R.id.usernameText);
        //Firebase authentication instance and listener
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Email_Password", "onAuthStateChanged:signed_in:" + user.getUid());
                    setHeader();

                } else {
                    // User is signed out
                    Log.d("Email_Password", "onAuthStateChanged:signed_out");
                    changeToLogin();
                }
                // ...
            }
        };

        mSignOutButton = (Button) findViewById(R.id.signoutBtn);
        mViewChatroomsButton = (Button) findViewById(R.id.viewChatroomsBtn);
        mCreateChatroomButton = (Button) findViewById(R.id.createChatroomBtn);

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        mViewChatroomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDisplayChatrooms();
            }
        });
        mCreateChatroomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToCreateChatroom();
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

    private void changeToDisplayChatrooms() {
        Intent intent = new Intent(this, DisplayChatrooms.class);
        startActivity(intent);
    }

    private void changeToCreateChatroom() {
        Intent intent = new Intent(this, CreateChatroom.class);
        startActivity(intent);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        changeToLogin();
    }

    private void setHeader() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("username").getRef();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                mUsernameText.setText(username);
                Toast.makeText(HomePage.this, "Welcome Back " + username, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
