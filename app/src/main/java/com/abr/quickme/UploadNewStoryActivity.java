package com.abr.quickme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class UploadNewStoryActivity extends AppCompatActivity {

    final String[] name = new String[1];
    private ImageView statusImage;
    private TextInputLayout statusText;
    private MaterialButton statusUpdateButton;
    private Bitmap d;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private String storyURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_new_story);

        String mCurrentUser = getIntent().getStringExtra("currentUser");//receive current user id

        statusImage = findViewById(R.id.story_image);
        statusText = findViewById(R.id.story_text);
        statusUpdateButton = findViewById(R.id.btn_story_update);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        statusUpdateButton.setEnabled(false);
        statusText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = statusText.getEditText().getText().toString().trim();
                // other stuffs
                if (text.isEmpty()) {
                    statusUpdateButton.setEnabled(false);
                } else {
                    statusUpdateButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        UploadStatus(mCurrentUser);//open camera/gallery to pic image for status

        Pix.start(UploadNewStoryActivity.this, Options.init().setRequestCode(100));
    }


    //select image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            ArrayList<String> returnValue;
            returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Log.d("U", "onActivityResult: " + returnValue.get(0));

            File f = new File(returnValue.get(0));
            d = new BitmapDrawable(getApplicationContext().getResources(), f.getAbsolutePath()).getBitmap();
            statusImage.setImageBitmap(d);
        }
    }

    //upload status(image caption)+ image
    private void UploadStatus(final String currentUser) {
        statusUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String storyText = statusText.getEditText().getText().toString();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                d.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                final StorageReference storyStorageRef = mStorageRef.child("stories/images/" + currentUser + "/" + currentUser + System.currentTimeMillis());

                //upload to storage
                UploadTask uploadTask = storyStorageRef.putBytes(imageData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        storyStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                storyURL = uri.toString();//download url of storyImage

                                //upload to database with caption
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Stories").child(currentUser);
                                mDatabase.child("name").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        name[0] = dataSnapshot.getValue().toString();//not working
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                HashMap<String, String> storyMap = new HashMap<>();
                                storyMap.put("storyText", storyText);
                                storyMap.put("storyImage", storyURL);
                                storyMap.put("name", name[0]);

                                mDatabase.setValue(storyMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(UploadNewStoryActivity.this, "Story posted!", Toast.LENGTH_SHORT).show();
                                            statusImage.setImageResource(0);
                                            statusText.getEditText().setText("");
                                            storyURL = "";
                                        } else {
                                            Toast.makeText(UploadNewStoryActivity.this, "Error posting story!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        });
                    }
                });

            }
        });
    }
}


