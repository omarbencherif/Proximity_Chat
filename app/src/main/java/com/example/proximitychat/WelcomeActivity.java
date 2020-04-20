package com.example.proximitychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button loginButton;
    Button createAccountButton;

    SharedPreferences prefs;


    String idToken;
    String idTokenJSON;

    Gson gson = new Gson();

    FirebaseAuth auth;

    Intent autoLoginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);



        prefs = this.getSharedPreferences("com.example.proximitychat", Context.MODE_PRIVATE);
        loginButton = findViewById(R.id.saveChanges);
        createAccountButton = findViewById(R.id.createAccountButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                WelcomeActivity.this.startActivity(myIntent);
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent createAccountIntent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
                WelcomeActivity.this.startActivity(createAccountIntent);
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            Intent autoLoginIntent = new Intent(WelcomeActivity.this, proximityChatActivity.class);
            startActivity(autoLoginIntent);
            finish();
        }

        //idTokenJSON = prefs.getString("Token", null);
        //idToken = gson.fromJson(idTokenJSON, String.class);


        //persistentLogin();
    }

    private void persistentLogin() {
        mAuth.signInWithCustomToken(idToken).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(WelcomeActivity.this, "Auto Sign-In Successful", Toast.LENGTH_SHORT).show();
                    Intent persistentLoginIntent = new Intent(WelcomeActivity.this, proximityChatActivity.class);
                    WelcomeActivity.this.startActivity(persistentLoginIntent);

                }
            }
        });
    }
}
