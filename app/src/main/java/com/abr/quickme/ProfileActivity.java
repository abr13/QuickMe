package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    ProgressDialog mProgressdialog;
    Button mProfileSendRequestButton, mRequestDeclineButton;
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private FirebaseUser mCurrentUser;

    private int friendCounts = 0;

    private String current_state;
    private String image;

    private AdView mAdViewBottom;

    private DatabaseReference mUsersDatabase, mFriendRequestDatabase, mFriendDatabase, mNotificationDatabase;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveProfilePic) {

            BitmapDrawable draw = (BitmapDrawable) mProfileImage.getDrawable();
            Bitmap bitmap = draw.getBitmap();

            FileOutputStream outStream = null;
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/Quick Me/Quick Me Profile Photos");
            dir.mkdirs();
            String fileName = String.format("%s.jpg", mProfileName.getText().toString() + "_" + System.currentTimeMillis());
            File outFile = new File(dir, fileName);
            try {
                outStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outFile));
                sendBroadcast(intent);
                Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Error, File not found!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    //show ad
    private void adView() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdViewBottom = findViewById(R.id.adViewBottomProfile);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewBottom.loadAd(adRequest);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.profile_save_menu, menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String selected_user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(selected_user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mProfileImage = findViewById(R.id.profileImageView);
        mProfileName = findViewById(R.id.profileName_textView);
        mProfileStatus = findViewById(R.id.profileStatus_textView);
        mProfileFriendsCount = findViewById(R.id.profileFriendCount_textView);
        mProfileSendRequestButton = findViewById(R.id.profileRequest_button);
        mRequestDeclineButton = findViewById(R.id.profileDeclineRequest_button);
        mProfileFriendsCount = findViewById(R.id.profileFriendCount_textView);

        mRequestDeclineButton.setVisibility(View.INVISIBLE);
        mRequestDeclineButton.setEnabled(false);

        adView();

        current_state = "not_friends";

        mProgressdialog = new ProgressDialog(this);
        mProgressdialog.setTitle("Loading Users Data...");
        mProgressdialog.setMessage("Want to have candle light dinner with your friend?");
        mProgressdialog.setCanceledOnTouchOutside(false);
        mProgressdialog.show();


        //show full screen image
        final ImagePopup imagePopup = new ImagePopup(this);
        imagePopup.setWindowHeight(800); // Optional
        imagePopup.setWindowWidth(800); // Optional
        imagePopup.setBackgroundColor(Color.BLACK);  // Optional
        imagePopup.setFullScreen(true); // Optional
        imagePopup.setHideCloseIcon(true);  // Optional
        imagePopup.setImageOnClickClose(true);// Optional

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(ProfileActivity.this, ImageViewActivity.class);
                imageIntent.putExtra("image", image);
                startActivity(imageIntent);

            }
        });

        mProfileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                registerForContextMenu(mProfileImage);

                return false;
            }
        });


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);
                Picasso.get().load(thumb_image).placeholder(R.drawable.profile_sample).into(mProfileImage);

                //prevent sending request to current user itself
                if (mCurrentUser.getUid().equals(selected_user_id)) {
                    mRequestDeclineButton.setVisibility(View.INVISIBLE);
                    mProfileSendRequestButton.setVisibility(View.INVISIBLE);
                }

                //count friendsfor the selected profile
                mFriendDatabase.child(selected_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int friendsCount = (int) dataSnapshot.getChildrenCount();
                        mProfileFriendsCount.setText("Total friends : " + friendsCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(selected_user_id)) {
                            String req_type = dataSnapshot.child(selected_user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                current_state = "req_received";
                                mProfileSendRequestButton.setText("Accept Request");

                                mRequestDeclineButton.setVisibility(View.VISIBLE);
                                mRequestDeclineButton.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                current_state = "req_sent";
                                mProfileSendRequestButton.setText("Cancel Friend Request");

                                mRequestDeclineButton.setVisibility(View.INVISIBLE);
                                mRequestDeclineButton.setEnabled(false);
                            }
                            mProgressdialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(selected_user_id)) {
                                                current_state = "friends";
                                                mProfileSendRequestButton.setText("Un Friend");


                                                mRequestDeclineButton.setVisibility(View.INVISIBLE);
                                                mRequestDeclineButton.setEnabled(false);
                                            }
                                            mProgressdialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            mProgressdialog.dismiss();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
///1///////////////////////////////////////
        mRequestDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (current_state.equals("req_received")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(selected_user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendRequestButton.setEnabled(true);
                                                    current_state = "not_friends";
                                                    mProfileSendRequestButton.setText("Send Request");
                                                    mRequestDeclineButton.setVisibility(View.INVISIBLE);
                                                    mRequestDeclineButton.setEnabled(false);
                                                    Toast.makeText(ProfileActivity.this, "Request Declined, you're rud!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            });
                }

            }
        });

        mProfileSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendRequestButton.setEnabled(false);

                //if not friends, send it
                if (current_state.equals("not_friends")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestDatabase.child(selected_user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        HashMap<String, String> notificationData = new HashMap<>();
                                                        notificationData.put("from", mCurrentUser.getUid());
                                                        notificationData.put("type", "request");
                                                        mNotificationDatabase.child(selected_user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                current_state = "req_sent";
                                                                mProfileSendRequestButton.setText("Cancel Friend Request");

                                                                mRequestDeclineButton.setVisibility(View.INVISIBLE);
                                                                mRequestDeclineButton.setEnabled(false);
                                                            }
                                                        });

                                                        Toast.makeText(ProfileActivity.this, "Request Sent, wait until he prepares dinner for you!", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Oops,Failed Sending Request", Toast.LENGTH_SHORT).show();
                                    }

                                    mProfileSendRequestButton.setEnabled(true);
                                }
                            });
                }


                //if friends, cancel it
                if (current_state.equals("req_sent")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(selected_user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendRequestButton.setEnabled(true);
                                                    current_state = "not_friends";
                                                    mProfileSendRequestButton.setText("Send Friend Request");
                                                    Toast.makeText(ProfileActivity.this, "Request Cancelled, you're rud!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            });
                }

                //received, accept it
                if (current_state.equals("req_received")) {

/////////////////////////////////////////////accept
                    mFriendDatabase.child(mCurrentUser.getUid()).child(selected_user_id).child("date").setValue("Friend since :" + " " + ServerValue.TIMESTAMP)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(selected_user_id).child(mCurrentUser.getUid()).child("date").setValue("Friend since :" + " " + ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_user_id).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mFriendRequestDatabase.child(selected_user_id).child(mCurrentUser.getUid()).removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    mProfileSendRequestButton.setEnabled(true);
                                                                                    current_state = "friends";
                                                                                    mProfileSendRequestButton.setText("Un Friend");

                                                                                    mRequestDeclineButton.setVisibility(View.INVISIBLE);
                                                                                    mRequestDeclineButton.setEnabled(false);

                                                                                    Toast.makeText(ProfileActivity.this, "Hohoo, Accepted, Party Started", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            });
                                                                }
                                                            });

                                                }
                                            });

                                }
                            });
                }

                //if friends unfriend it
                if (current_state.equals("friends")) {
                    mFriendDatabase.child(mCurrentUser.getUid()).child(selected_user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(selected_user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {


                                                    final DatabaseReference mRootRef;
                                                    mRootRef = FirebaseDatabase.getInstance().getReference();
                                                    mRootRef.child("Messages").child(mCurrentUser.getUid()).child(selected_user_id).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mRootRef.child("Messages").child(selected_user_id).child(mCurrentUser.getUid()).removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    mRootRef.child("Chat").child(selected_user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            mRootRef.child("Chat").child(mCurrentUser.getUid()).child(selected_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    Toast.makeText(getApplicationContext(), "All messages and chat deleted!", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                }
                                                            });


                                                    mProfileSendRequestButton.setEnabled(true);
                                                    current_state = "not_friends";
                                                    mProfileSendRequestButton.setText("Send Friend Request");
                                                    Toast.makeText(ProfileActivity.this, "Un Friend Done, you're rud!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            });
                }
            }
        });

    }
}
