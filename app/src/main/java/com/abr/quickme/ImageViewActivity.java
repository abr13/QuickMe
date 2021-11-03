package com.abr.quickme;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView fullScreenImage;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        String image = getIntent().getStringExtra("image");

        mToolbar = findViewById(R.id.profile_settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Image");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        fullScreenImage = findViewById(R.id.fullScreenImage);

        Picasso.get().load(image).placeholder(R.drawable.profile_sample).into(fullScreenImage);
    }
}
