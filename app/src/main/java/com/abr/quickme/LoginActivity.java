package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout textlogEmail, textlogPassword;
    Button btn_login;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRegProgress = new ProgressDialog(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login An Existing Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textlogEmail = findViewById(R.id.textlogEmail);
        textlogPassword = findViewById(R.id.textlogPassword);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = textlogEmail.getEditText().getText().toString();
                String password = textlogPassword.getEditText().getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mRegProgress.setTitle("Signing...");
                    mRegProgress.setMessage("Your coffee is getting ready !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    signin_user(email, password);
                } else {
                    textlogEmail.getEditText().setError("Enter email");
                    textlogPassword.getEditText().setError("Enter password");
                }
            }
        });
    }

    private void signin_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mRegProgress.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            Toast.makeText(LoginActivity.this, "Error logging in!", Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }
}
