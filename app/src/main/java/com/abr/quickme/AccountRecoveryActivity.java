package com.abr.quickme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class AccountRecoveryActivity extends AppCompatActivity {

    Button resetBtn;
    TextInputLayout emailText;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_recovery);


        mToolbar = findViewById(R.id.recovery_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset Your Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resetBtn = findViewById(R.id.reset_btn);
        emailText = findViewById(R.id.resetEmail);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (TextUtils.isEmpty(emailText.getEditText().getText().toString())) {
                    Snackbar snackbar_su = Snackbar.make(v, "Please enter your email!", Snackbar.LENGTH_LONG);
                    snackbar_su.show();
                } else {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.sendPasswordResetEmail(emailText.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                emailText.getEditText().setText("");
                                Snackbar snackbar_su = Snackbar.make(v, "Recovery email has been sent to you email", Snackbar.LENGTH_LONG);
                                snackbar_su.show();
                            } else {
                                Snackbar snackbar_su = Snackbar.make(v, task.getException().getMessage(), Snackbar.LENGTH_LONG);
                                snackbar_su.show();
                            }
                        }
                    });
                }
            }
        });

    }
}
