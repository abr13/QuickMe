package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    TextInputLayout textlogEmail, textlogPassword;
    MaterialButton btn_login;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    int RC_SIGN_IN = 0;
    private String email;
    private ProgressDialog mRegProgress, mRegProgress1;
    private SignInButton googleSigninBtn;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRegProgress = new ProgressDialog(this);
        mRegProgress1 = new ProgressDialog(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login With An Existing Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textlogEmail = findViewById(R.id.textlogEmail);
        textlogPassword = findViewById(R.id.textlogPassword);
        btn_login = findViewById(R.id.btn_login);

        googleSigninBtn = findViewById(R.id.googleSigninBtn);
        googleSigninBtn.setSize(SignInButton.SIZE_ICON_ONLY);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegProgress1.setTitle("Signing with Google...");
                mRegProgress1.setMessage("Your coffee is getting ready !");
                mRegProgress1.setCanceledOnTouchOutside(false);
                mRegProgress1.show();
                signIn();
            }
        });

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

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(LoginActivity.this, "Google signin success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            String name = user.getDisplayName();
                            String image = user.getPhotoUrl().toString();
                            String uid = user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("status", "It's Quick Messaging, Make it Quick!");
                            userMap.put("image", image);
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mRegProgress.dismiss();
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Google signin failed!", Toast.LENGTH_SHORT).show();
                            mRegProgress1.dismiss();
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
