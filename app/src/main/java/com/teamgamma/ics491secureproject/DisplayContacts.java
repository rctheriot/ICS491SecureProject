package com.teamgamma.ics491secureproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DisplayContacts extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ListView mUserList;

    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mKeys = new ArrayList<>();

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contacts);

        user = FirebaseAuth.getInstance().getCurrentUser();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mUsernames);
        if (user != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid()).getRef();
            mUserList = (ListView) findViewById(R.id.userList);
            mUserList.setAdapter(arrayAdapter);
        }

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.child("Name").getValue(String.class);
                String email = dataSnapshot.child("Email").getValue(String.class);
                mUsernames.add(name + "  -  " + email);
                String key = dataSnapshot.getKey();
                mKeys.add(key);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.child("Name").getValue(String.class);
                String email = dataSnapshot.child("Email").getValue(String.class);
                mUsernames.add(name + "  -  " + email);
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);
                mUsernames.set(index, name + "  -  " + email);
                arrayAdapter.notifyDataSetChanged();

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
