package com.abr.quickme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartActivity extends AppCompatActivity {

    MaterialButton btn_create, btn_login;
    CircleImageView logo;
    TextView tagLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btn_create = findViewById(R.id.btn_create);
        btn_login = findViewById(R.id.btn_login);

        logo = findViewById(R.id.logo);
        tagLine = findViewById(R.id.tagLine);

        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.start_transition);
        logo.startAnimation(myanim);
        tagLine.startAnimation(myanim);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(logIntent);
            }
        });
    }

}
