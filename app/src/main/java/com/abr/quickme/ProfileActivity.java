package com.abr.quickme;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    ProgressDialog mProgressdialog;
    Button mProfileSendRequestButton, mRequestDeclineButton;
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private FirebaseUser mCurrentUser;

    private String current_state;

    private DatabaseReference mUsersDatabase, mFriendRequestDatabase, mFriendDatabase, mNotificationDatabase;

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

        mRequestDeclineButton.setVisibility(View.INVISIBLE);
        mRequestDeclineButton.setEnabled(false);


        current_state = "not_friends";


        mProgressdialog = new ProgressDialog(this);
        mProgressdialog.setTitle("Loading Users Data...");
        mProgressdialog.setMessage("Want to have candle light dinner with your friend?");
        mProgressdialog.setCanceledOnTouchOutside(false);
        mProgressdialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.profile_sample2).into(mProfileImage);

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
                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(selected_user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(selected_user_id).child(mCurrentUser.getUid()).setValue(currentDate)
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
