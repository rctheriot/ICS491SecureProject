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

/**
 * Homepage Activity
 * This is the activity the user views after he logins in
 */
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

        //Set title of page
        setTitle("Homepage");

        //Get Firebase Authentication Instance
        mAuth = FirebaseAuth.getInstance();

        //Firebase Authentication State Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //If the user is not null then show the page, else change to Login Activity
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setHeader();

                } else {
                    changeToLogin();
                }
                // ...
            }
        };

        //Bind username to TextView
        mUsernameText = (TextView) findViewById(R.id.usernameText);

        //Bind SignOut button to signOut Function
        mSignOutButton = (Button) findViewById(R.id.signoutBtn);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        //Bind Display Chatrooms button to changeToDisplayChatrooms Function
        mViewChatroomsButton = (Button) findViewById(R.id.viewChatroomsBtn);
        mViewChatroomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDisplayChatrooms();
            }
        });

        //Bind Create Chatroom button to changeToCreateChatroom Function
        mCreateChatroomButton = (Button) findViewById(R.id.createChatroomBtn);
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

    /**
     * Change to Login page
     */
    private void changeToLogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    /**
     * Change to Chatroom List Page
     */
    private void changeToDisplayChatrooms() {
        Intent intent = new Intent(this, DisplayChatrooms.class);
        startActivity(intent);
    }

    /**
     * Change to CreateChatroom Page
     */
    private void changeToCreateChatroom() {
        Intent intent = new Intent(this, CreateChatroom.class);
        startActivity(intent);
    }

    /**
     * Sign User Out
     */
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        changeToLogin();
    }

    /**
     * Sets the header of the page to the user's Username
     */
    private void setHeader() {

        //Get the Username reference from Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("username").getRef();

        //Set value of header to username
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
