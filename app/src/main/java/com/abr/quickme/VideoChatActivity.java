package com.abr.quickme;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener,
        PublisherKit.PublisherListener {

    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private static String API_KEY = "46695532";
    private static String SESSION_ID = "2_MX40NjY5NTUzMn5-MTU4NzYzNzMzNzY5MH5PKzZyM08zeUxNejQrSDVabEsxczIvT1N-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjY5NTUzMiZzaWc9MDUzZWVkZDc3NDcxOGY4NzkwYTljZjEzMWI5Zjk1MWQxZDczOWJkZDpzZXNzaW9uX2lkPTJfTVg0ME5qWTVOVFV6TW41LU1UVTROell6TnpNek56WTVNSDVQS3paeU0wOHplVXhOZWpRclNEVmFiRXN4Y3pJdlQxTi1mZyZjcmVhdGVfdGltZT0xNTg3NjM3NDQwJm5vbmNlPTAuOTIyMTcwNTI0NDE2MDk0NyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkwMjI5NDM3JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";

    private FrameLayout mPublisherView;
    private FrameLayout mSubscriberView;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView endCallButton;
    private DatabaseReference userRef;
    private String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        //String mCallerUserId = getIntent().getStringExtra("user_id");
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        endCallButton = findViewById(R.id.end_videocall_button);
        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("ringing")) {
                            userRef.child(userID).child("ringing").removeValue();

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            finish();

                        }
                        if (dataSnapshot.child(userID).hasChild("calling")) {
                            userRef.child(userID).child("calling").removeValue();
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            finish();
                        } else {
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
                startActivity(new Intent(VideoChatActivity.this, MainActivity.class));
                finish();

            }

        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            mPublisherView = findViewById(R.id.publisher_container);
            mSubscriberView = findViewById(R.id.subscriber_container);

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "Please allow Microphone and Camera permissions.", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //publish stream
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "onConnected: session connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherView.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Toast.makeText(this, "Call ended!", Toast.LENGTH_LONG).show();

    }

    //sub recv
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "onStreamReceived: ");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberView.addView(mSubscriber.getView());
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "onStreamDropped: ");

        if (mSubscriber != null) {
            mSubscriberView.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "onError: ");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
