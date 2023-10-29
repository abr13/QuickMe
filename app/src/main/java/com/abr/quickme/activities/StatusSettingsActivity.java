package com.abr.quickme.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.abr.quickme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusSettingsActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    Toolbar mToolbar;
    Button btn_changeStatus;
    TextInputLayout textStatus;
    ProgressDialog mProgress;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_settings);

        btn_changeStatus = findViewById(R.id.btn_changeStatus);
        textStatus = findViewById(R.id.textStatus);
        mToolbar = findViewById(R.id.StatusSettings_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        textStatus.getEditText().setText(status_value);

        btn_changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(StatusSettingsActivity.this);
                mProgress.setTitle("Updating");
                mProgress.setMessage("Nice status, wait until we make it public");
                mProgress.show();

                String status = textStatus.getEditText().getText().toString();

                mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                String current_uid = mCurrentUser.getUid();
                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).child("status");
                mUserDatabase.setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgress.dismiss();
                        Toast.makeText(StatusSettingsActivity.this, "Status Updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
