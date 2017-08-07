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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Signin Activity
 * This is the activity the user views when needs to signin and when he first starts the app
 */
public class Login extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;

    private Button mSignInButton;
    private Button mCreateAccountButton;

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get Firebase Authentication Instance
        mAuth = FirebaseAuth.getInstance();

        //Firebase Authentication State Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Get the user currently signed in
                user = firebaseAuth.getCurrentUser();

                //If the user is not null then show the Home Page, else ask user to login
                if (user != null) {
                    changeToHomeScreen();

                } else {
                    Toast.makeText(Login.this, "Sign In or Create an Account", Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        };

        // Bind Email/Password EditText Fields
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);

        //Bind Sigin button to signIn Function
        mSignInButton = (Button) findViewById(R.id.signinBtn);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //Bind Sigin button to changeToCreateAccount Function
        mCreateAccountButton = (Button) findViewById(R.id.createAccountBtn);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToCreateAccount();
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
     * Change to Homepage
     */
    private void changeToHomeScreen() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

    /**
     * Change to CreateAccount page
     */
    private void changeToCreateAccount() {
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    /**
     * Sign in the User using Firebase Login API
     */
    private void signIn() {

        //Get  email and password from EditText fields
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        //Check if email or password fields are not lenght 0
        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(Login.this, "Please enter a valid Email/Password", Toast.LENGTH_SHORT).show();
            return;
        }

        //Signin Through firebase API
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //If login successful change to homescreen, else display error message
                        if (!task.isSuccessful()) {
                            Toast.makeText(Login.this, "Failed to Login", Toast.LENGTH_SHORT).show();
                        } else {
                            changeToHomeScreen();
                        }

                    }
                });
    }


}
