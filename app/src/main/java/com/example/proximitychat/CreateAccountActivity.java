package com.example.proximitychat;

import android.content.Intent;
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

import java.util.regex.Pattern;

public class CreateAccountActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private FirebaseAuth mAuth;

    private String email;
    private String pass;

    private String confirmEmail;
    private String confirmPass;

    private EditText emailText;
    private EditText confirmEmailText;

    private EditText passText;
    private EditText confirmPasswordText;

    private TextView emailError;
    private TextView passwordError;


    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();


        setContentView(R.layout.activity_create_account);
        Button loginButton = (Button) findViewById(R.id.saveChanges);
        passwordError = (TextView) findViewById(R.id.passwordError);
        emailError = (TextView) findViewById(R.id.display_name_error);

        emailError.setVisibility(View.INVISIBLE);
        passwordError.setVisibility(View.INVISIBLE);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText = (EditText) findViewById(R.id.editEmail);
                passText = (EditText) findViewById(R.id.editPassword);

                confirmEmailText = (EditText) findViewById(R.id.editConfirmEmail);
                confirmPasswordText = (EditText) findViewById(R.id.editConfirmPassword);


                confirmEmail = confirmEmailText.getText().toString();
                confirmPass = confirmPasswordText.getText().toString();

                email = emailText.getText().toString();
                pass = passText.getText().toString();

                createAccount(email, confirmEmail, pass, confirmPass);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    private void createAccount(String email, String confirmEmail, String password, String confirmPass) {
        Log.d(TAG, "createAccount:" + email);


        if (!validateEmail(email, confirmEmail) || (!validatePass(password, confirmPass))) {
            return;
        }

        showProgressBar();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password).

                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(CreateAccountActivity.this, String.format("User %s successfully created.", user),
                                    Toast.LENGTH_SHORT).show();

                            //updateUI(user);

                            Intent loginIntent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                            CreateAccountActivity.this.startActivity(loginIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(CreateAccountActivity.this, "User creation failed. Your E-mail address might already be in use.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
    }



    private boolean validateEmail(String email, String confirmEmail) {
        boolean flag = true;


        if (!email.equals(confirmEmail)) {
            emailError.setText(R.string.email_confirm_error);
            flag = false;
        } else if (email.length() < 3) {
            emailError.setText(R.string.email_length_error);
            flag = false;
        } else if (email.length() > 64) {
            emailError.setText(R.string.email_max_length_error);
            flag = false;
        }

        if (!flag) {
            emailError.setVisibility(View.VISIBLE);
        } else {
            emailError.setVisibility(View.INVISIBLE);
        }


        return flag;


    }


    private boolean validatePass(String password, String confirmPass) {
        Pattern specialCharPattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern digitCharPatten = Pattern.compile("[0-9 ]");
        Pattern upperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");


        boolean flag = true;


        if (password.length() < 8) {

            passwordError.setText((R.string.password_length_error));
            flag = false;
        } else if (!specialCharPattern.matcher(password).find()) {
            passwordError.setText((R.string.password_special_error));
            flag = false;
        } else if (!upperCasePatten.matcher(password).find()) {
            passwordError.setText((R.string.password_upper_error));
            flag = false;
        } else if (!lowerCasePatten.matcher(password).find()) {
            passwordError.setText((R.string.password_lower_error));
            flag = false;
        } else if (!digitCharPatten.matcher(password).find()) {
            passwordError.setText((R.string.password_digit_error));
            flag = false;
        } else if (!password.equals(confirmPass)) {
            passwordError.setText(R.string.password_confirm_error);
            flag = false;
        }


        if (!flag) {
            passwordError.setVisibility(View.VISIBLE);
        } else {
            passwordError.setVisibility(View.INVISIBLE);
        }

        return flag;
    }
}