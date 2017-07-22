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
import com.google.firebase.database.DatabaseReference;

public class Login extends AppCompatActivity {

    //Firebase database and autentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Text Fields
    private EditText mEmailField;
    private EditText mPasswordField;

    //Buttons
    private Button mSignInButton;
    private Button mCreateAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase authentication instance and listener
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Email_Password", "onAuthStateChanged:signed_in:" + user.getUid());
                    changeToHomeScreen();

                } else {
                    // User is signed out
                    Log.d("Email_Password", "onAuthStateChanged:signed_out");
                    Toast.makeText(Login.this, "Sign In or Create an Account", Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        };

        // Link Email/Password Fields
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);

        //Link buttons
        mSignInButton = (Button) findViewById(R.id.signinBtn);

        mCreateAccountButton = (Button) findViewById(R.id.createAccountBtn);

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

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

    private void changeToHomeScreen() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

    private void changeToCreateAccount() {
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    private void signIn() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        //Check if email or password fields are 0
        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(Login.this, "Please enter a valid Email/Password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("EmailPassword", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(Login.this, "Failed to Login",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            changeToHomeScreen();
                        }

                    }
                });
    }


}
