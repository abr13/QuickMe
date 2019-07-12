package com.abr.quickme;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDisplayId = findViewById(R.id.sampleText);
        String user_id = getIntent().getStringExtra("user_id");
        mDisplayId.setText(user_id);

    }
}
