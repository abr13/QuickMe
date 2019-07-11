package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SETTINGS_ACTIVITY";
    FirebaseUser mCurrentUser;
    CircleImageView settings_profile_image;

    Toolbar mToolbar;
    ProgressDialog mProgress;
    private DatabaseReference mUserDatabase;
    private CircleImageView mDislplayImage;
    private TextView mName, mStatus;
    private StorageReference mStorageRef, mImageStorage;
    private Button btn_statusSettings, btn_changeImage;
    private String ImgURL;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDislplayImage = findViewById(R.id.settings_profile_image);
        mName = findViewById(R.id.settings_displayName);
        mStatus = findViewById(R.id.settings_Status);

        fetchProfile();

        btn_changeImage = findViewById(R.id.btn_changeImage);
        btn_changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();

                mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                final String current_uid = mCurrentUser.getUid();

                final StorageReference filePath = mImageStorage.child("profile_images/").child(current_uid + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ImgURL = uri.toString();

                                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).child("image");
                                mUserDatabase.setValue(ImgURL).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SettingsActivity.this, "Hohoo, Image Updated", Toast.LENGTH_SHORT).show();
                                        settings_profile_image = findViewById(R.id.settings_profile_image);
                                        settings_profile_image.setImageURI(resultUri);
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Oops2, Something Went Wrong", Toast.LENGTH_LONG).show();

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Oops, Error Cropping Image", Toast.LENGTH_SHORT).show();
            }
        }
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

                Picasso.get().load(image).into(mDislplayImage);
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
