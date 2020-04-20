package com.example.proximitychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import java.util.Date;
import java.util.Objects;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    FirebaseUser user;


    Button loginButton;
    String token;
    String email;
    String password;

    EditText editEmail;
    TextView editPassword;

    EditText emailTextView;
    EditText passwordTextView;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = this.getSharedPreferences("com.example.proximitychat", Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.button_login);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emailTextView = (EditText) findViewById(R.id.editEmail);
                passwordTextView = (EditText) findViewById(R.id.editPassword);


                email = emailTextView.getText().toString();
                password = passwordTextView.getText().toString();

                signIn(email, password);
            }
        });
    }

    @Override
    public void onClick(View v) {

    }


    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        showProgressBar();



        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    Toast.makeText(LoginActivity.this, "Login Successful",
                            Toast.LENGTH_SHORT).show();

                    user = mAuth.getCurrentUser();

                    SharedPreferences.Editor editor = prefs.edit();




                    Gson gson = new Gson();

                    String json = gson.toJson(Objects.requireNonNull(user).getIdToken(true));
                    editor.putString("Token", json);
                    editor.commit();


                    //updateUI(user);

                    Intent proximityChatIntent = new Intent(getApplicationContext(), proximityChatActivity.class);
                    proximityChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    LoginActivity.this.startActivity(proximityChatIntent);
                    finish();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, R.string.login_failed,
                            Toast.LENGTH_SHORT).show();
                    //updateUI(null);
                }

                // [START_EXCLUDE]
                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, R.string.login_failed,
                            Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
                // [END_EXCLUDE]
            }
        });
        // [END sign_in_with_email]
    }


}
