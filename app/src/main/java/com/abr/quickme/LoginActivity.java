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

import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    TextInputLayout textlogEmail, textlogPassword;
    MaterialButton btn_login;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    int RC_SIGN_IN = 0;
    private String email;
    CallbackManager mCallbackManager;
    private SignInButton googleSigninBtn;
    private ProgressDialog mRegProgress, mRegProgress1, mRegProgress2;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;
    private LoginButton facebookSigninBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRegProgress = new ProgressDialog(this);
        mRegProgress1 = new ProgressDialog(this);
        mRegProgress2 = new ProgressDialog(this);
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

        facebookSigninBtn = findViewById(R.id.facebookSigninBtn);

        mCallbackManager = CallbackManager.Factory.create();
        //check login for facebook

        facebookSigninBtn.setReadPermissions("email", "public_profile");
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        facebookSigninBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                mRegProgress2.setTitle("Signing with Facebook...");
                mRegProgress2.setMessage("Your coffee is getting ready !");
                mRegProgress2.setCanceledOnTouchOutside(false);
                mRegProgress2.show();

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
// ...


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
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

                            final String name = user.getDisplayName();
                            final String image = user.getPhotoUrl().toString();
                            String uid = user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        mRegProgress.dismiss();
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    } else {
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
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

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

    //facebook


    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            loadUserProfile(token, user.getUid());


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mRegProgress.dismiss();
                        }

                        // ...
                    }
                });
    }


    private void loadUserProfile(AccessToken newAccessToken, final String uid) {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    final String first_name = object.getString("first_name");
                    final String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    final String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mRegProgress.dismiss();
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                                finish();
                            } else {
                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("name", first_name + " " + last_name);
                                userMap.put("status", "It's Quick Messaging, Make it Quick!");
                                userMap.put("image", image_url);
                                userMap.put("thumb_image", "default");

                                mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mRegProgress.dismiss();
                                            mRegProgress.dismiss();
                                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();


                } catch (JSONException e) {
                    mRegProgress.dismiss();
                    e.printStackTrace();
                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    //faceboob

}
