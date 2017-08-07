package com.teamgamma.ics491secureproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Create Account Activity
 * This is the activity the user views when he wants to create a new user account
 */
public class CreateAccount extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mUsernameField;

    private Button mCreateAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Set title on Activity
        setTitle("Create Account");

        //Get Firebase Authentication Instance
        mAuth = FirebaseAuth.getInstance();

        //Firebase Authentication State Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is Logged In
                } else {
                    //User is Logged Out
                }
            }
        };

        //Bind private variables to the EditText fields
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mUsernameField = (EditText) findViewById(R.id.usernameField);

        //Bind Create button to createAccount Functions
        mCreateAccountButton = (Button) findViewById(R.id.createAccountBtn);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
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
     * Change activity to HomeScreen
     */
    private void changeToHomeScreen() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

    /**
     * Validates the user password with regular expressions
     */
    private class PasswordValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String PASSWORD_PATTERN =
                "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

        public PasswordValidator() {
            pattern = Pattern.compile(PASSWORD_PATTERN);
        }

        /**
         * Validate password with regular expression
         *
         * @param password password for validation
         * @return true valid password, false invalid password
         */
        public boolean validate(final String password) {

            matcher = pattern.matcher(password);
            return matcher.matches();

        }
    }


    /**
     * Adds the username to the Database
     * @param username
     */
    private void addUsername(String username) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).getRef();
        mDatabase.child("username").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreateAccount.this, "", Toast.LENGTH_LONG).show();
                    changeToHomeScreen();
                } else {
                    Toast.makeText(CreateAccount.this, "Failed to Add", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * Creates a new account within the database using the Firebase Email/Password API
     */
    private void createAccount() {

        //Get all info from the EditText fields
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        PasswordValidator valid = new PasswordValidator();

        //Validate the Password
        if (!valid.validate(password)) {
            Toast.makeText(CreateAccount.this, "Password invalid.", Toast.LENGTH_SHORT).show();
            Toast.makeText(CreateAccount.this, "Must contain (1)lowercase, (1)uppercase, (1)special symbol minimum 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        //Create Account with Firebase Email/Password API call
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("CreateAccount", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccount.this, "Failed to Create Account", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CreateAccount.this, "Account Created", Toast.LENGTH_SHORT).show();
                            String username = mUsernameField.getText().toString();
                            //Add the username to the database
                            addUsername(username);
                        }

                    }
                });

    }
}
