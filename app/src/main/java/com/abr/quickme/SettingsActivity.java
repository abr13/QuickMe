package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    Toolbar mToolbar;
    ProgressDialog mProgress;
    private DatabaseReference mUserDatabase;
    private CircleImageView mDislplayImage;
    private TextView mName, mStatus;
    private Button btn_statusSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDislplayImage = findViewById(R.id.settings_profile_image);
        mName = findViewById(R.id.settings_displayName);
        mStatus = findViewById(R.id.settings_Status);

        fetchProfile();

        btn_statusSettings = findViewById(R.id.btn_status_settings);
        btn_statusSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusSettingsIntent = new Intent(SettingsActivity.this, StatusSettingsActivity.class);
                String status_value = mStatus.getText().toString();
                statusSettingsIntent.putExtra("status_value", status_value);
                startActivity(statusSettingsIntent);
            }
        });
    }

    private void fetchProfile() {
        mProgress = new ProgressDialog(SettingsActivity.this);
        mProgress.setTitle("Fetching Profile");
        mProgress.setMessage("Your Toast Is Being Ready!");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                mProgress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
