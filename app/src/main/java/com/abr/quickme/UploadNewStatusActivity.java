package com.abr.quickme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;

import java.io.File;
import java.util.ArrayList;

public class UploadNewStatusActivity extends AppCompatActivity {


    private ImageView statusImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_new_status);

        String mCurrentUser = getIntent().getStringExtra("currentUser");

        statusImage = findViewById(R.id.status_image);

        Pix.start(UploadNewStatusActivity.this, Options.init().setRequestCode(100));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            ArrayList<String> returnValue;
            returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Log.d("U", "onActivityResult: " + returnValue.get(0));

            File f = new File(returnValue.get(0));
            Bitmap d = new BitmapDrawable(getApplicationContext().getResources(), f.getAbsolutePath()).getBitmap();
            statusImage.setImageBitmap(d);


        }
    }
}


