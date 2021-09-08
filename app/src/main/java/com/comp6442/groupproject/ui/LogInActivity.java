package com.comp6442.groupproject.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.comp6442.groupproject.BuildConfig;
import com.comp6442.groupproject.R;
import com.comp6442.groupproject.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
  EditText ed1, ed2;
  Button b1;
  private FirebaseAuth mAuth;

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Initialize Firebase Auth
    mAuth = FirebaseAuth.getInstance();
    if (BuildConfig.DEBUG) {
      try {
        mAuth.useEmulator("10.0.2.2", 9099);
      } catch (IllegalStateException exc) {
        Timber.w(exc);
      }
    }

    // UI
    ed1 = findViewById(R.id.login_form_email);
    ed2 = findViewById(R.id.login_form_password);
    b1 = findViewById(R.id.login_button);
    b1.setEnabled(true);
    b1.setOnClickListener(LogInActivity.this);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Check if user is signed in (non-null) and update UI accordingly.
    FirebaseUser user = mAuth.getCurrentUser();

    if (user != null) {
      Timber.d("User already logged in: %s. Taking user to home..", user.getEmail());
      home(user);
    }
  }

  @Override
  public void onClick(View view) {
    String username = ed1.getText().toString();
    String password = ed2.getText().toString();
    signIn(username, password);
  }

  @SuppressLint("TimberArgCount")
  private void signIn(String email, String password) {
    mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
              if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                if (firebaseUser != null) {
                  Timber.d("Sign in successful: %s", firebaseUser.getEmail());
                  home(firebaseUser);
                }
              } else {
                // If sign in fails, display a message to the user.
                Timber.w(task.getException(), "Failed to sign in");
                if (BuildConfig.DEBUG) {
                  Toast.makeText(LogInActivity.this, "Sign in failed.",
                          Toast.LENGTH_SHORT).show();
                }
                // this is required to clear the password form
                ed2.setText(" ");
                ed2.setText("");
              }
            });
  }

  @SuppressLint("TimberArgCount")
  private void createAccount(String email, String password) {
    mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
              if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                Timber.i("Successfully created account: %s", firebaseUser.getEmail());
                Toast.makeText(LogInActivity.this, "Success", Toast.LENGTH_SHORT).show();
                UserRepository.getInstance().addUser(firebaseUser);
                home(firebaseUser);
              } else {
                // If sign in fails, display a message to the user.
                Timber.w(task.getException(), "Failed to create account");
                Toast.makeText(LogInActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void home(FirebaseUser firebaseUser) {
    if (firebaseUser == null) Timber.w("Error, could not fetch current user");
    else {
      // take user to app home screen
      Timber.d("Taking user to app home screen %s", firebaseUser.getUid());
      Intent intent = new Intent(this, MainActivity.class);
      intent.putExtra("uid", firebaseUser.getUid());
      startActivity(intent);
    }
  }
}