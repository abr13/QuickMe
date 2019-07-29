package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    TextInputLayout textlogEmail, textlogPassword;
    MaterialButton btn_login;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;
    private String email;

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

        final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        textlogEmail.getEditText().addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {


            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // other stuffs
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email = textlogEmail.getEditText().getText().toString().trim();
                // other stuffs
                if (email.matches(emailPattern)) {
                    btn_login.setEnabled(true);
                } else {
                    textlogEmail.getEditText().setError("Invalid email");
                    btn_login.setEnabled(false);
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String email = textlogEmail.getEditText().getText().toString();
                String password = textlogPassword.getEditText().getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mRegProgress.setTitle("Signing...");
                    mRegProgress.setMessage("Your coffee is getting ready !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    signin_user(email, password, v);
                } else {
                    textlogEmail.getEditText().setError("Enter email");
                    textlogPassword.getEditText().setError("Enter password");
                }
            }
        });
    }

    private void signin_user(String email, String password, final View v) {
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
                            Snackbar snackbar_su = Snackbar.make(v, "Something is wrong!", Snackbar.LENGTH_LONG);
                            snackbar_su.show();
                        }
                        // ...
                    }
                });


    }

    //forgotten_password
    public void onClick(View v) {
        if (v.getId() == R.id.forgotten_password) {
            Intent accountRecoveryIntent = new Intent(LoginActivity.this, AccountRecoveryActivity.class);
            startActivity(accountRecoveryIntent);
        }
        if (v.getId() == R.id.donthaveAccount) {
            Intent newAccountIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(newAccountIntent);
        }
    }

}
