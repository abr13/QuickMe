package com.abr.quickme.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.abr.quickme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    MaterialButton changePasswordBtn;
    TextInputLayout oldPasswordText, newPasswordText, cNewPasswordText;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mToolbar = findViewById(R.id.changePassword_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Your Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changePasswordBtn = findViewById(R.id.btn_ChangePassword);
        oldPasswordText = findViewById(R.id.textCurrentPassword);
        newPasswordText = findViewById(R.id.textNewPassword);
        cNewPasswordText = findViewById(R.id.textConfirmNewPassword);

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (TextUtils.isEmpty(oldPasswordText.getEditText().getText().toString()) ||
                        TextUtils.isEmpty(newPasswordText.getEditText().getText().toString()) ||
                        TextUtils.isEmpty(cNewPasswordText.getEditText().getText().toString())) {
                    Snackbar snackbar_su = Snackbar.make(v, "Fields are empty!", Snackbar.LENGTH_LONG);
                    snackbar_su.show();
                } else if (newPasswordText.getEditText().getText().toString().equals(cNewPasswordText.getEditText().getText().toString())) {
                    String oldpass = oldPasswordText.getEditText().getText().toString();
                    final String newPass = newPasswordText.getEditText().getText().toString();

                    final FirebaseUser user;
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    final String email = user.getEmail();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, oldpass);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Snackbar snackbar_fail = Snackbar.make(v, "Something went wrong. Please try again later", Snackbar.LENGTH_LONG);
                                            snackbar_fail.show();
                                        } else {
                                            Snackbar snackbar_su = Snackbar.make(v, "Password Successfully Modified", Snackbar.LENGTH_LONG);
                                            snackbar_su.show();
                                            oldPasswordText.getEditText().setText("");
                                            newPasswordText.getEditText().setText("");
                                            cNewPasswordText.getEditText().setText("");
                                        }
                                    }
                                });
                            } else {
                                Snackbar snackbar_su = Snackbar.make(v, "Authentication Failed", Snackbar.LENGTH_LONG);
                                snackbar_su.show();
                            }
                        }
                    });
                } else {
                    Snackbar snackbar_su = Snackbar.make(v, "New passwords is not matching!", Snackbar.LENGTH_LONG);
                    snackbar_su.show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}