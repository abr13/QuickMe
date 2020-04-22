package com.abr.quickme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abr.quickme.classes.GetTimeAgo;
import com.abr.quickme.classes.TelegramBottomPanel;
import com.abr.quickme.models.Messages;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.scottyab.aescrypt.AESCrypt;
import com.squareup.picasso.Picasso;

import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.instachat.emojilibrary.model.layout.EmojiCompatActivity;
import br.com.instachat.emojilibrary.model.layout.TelegramPanelEventListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends EmojiCompatActivity implements TelegramPanelEventListener {

    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private static final String TAG = "CHAT ACTIVITY";
    private final List<Messages> messagesList = new ArrayList<>();
    private Toolbar mChatToolbar;
    private String mChatUserId, mChatUserName;
    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImage;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private TelegramBottomPanel mBottomPanel;

    private String fileType = "", myUrl = "";
    private Uri fileUrl;
    private StorageTask uploadtask;
    private ProgressDialog mProgressDialog;

    // function to generate a random string of length n (encryption key)
    static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    private void sendMessage() {

        String message = mBottomPanel.getText();
        if (!message.trim().equals("")) {

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            final String currentDateTime = dateFormat.format(date);

            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUserId;
            String chat_user_ref = "Messages/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserId)
                    .child(mChatUserId)
                    .push();
            String push_id = user_message_push.getKey();

//            //Encrypt Message Here
            String key = getAlphaNumericString(20);
            String encryptedMsg = "";

            try {
                encryptedMsg = AESCrypt.encrypt(key, message.trim());
                Log.d(TAG, "sendMessage: " + encryptedMsg);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

            Map messageMap = new HashMap();
            messageMap.put("message", encryptedMsg);
            messageMap.put("seen", "false");
            messageMap.put("type", "text");
            messageMap.put("time", currentDateTime);
            messageMap.put("from", mCurrentUserId);
            messageMap.put("to", mChatUserId);
            messageMap.put("key", key);
            messageMap.put("message_id", push_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mBottomPanel.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT LOG ", databaseError.getMessage());
                    }
                }
            });

        }

    }

    public void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUserId);
        Query messageQuary = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuary.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                messagesList.add(message);

                mAdapter.notifyDataSetChanged();

                //mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(itemPos, 0);
                mMessagesList.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUserId);
        Query messageQuary = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuary.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = messageKey;
                }
                if (itemPos == 1) {

                    mLastKey = messageKey;
                }

                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mProgressDialog = new ProgressDialog(this);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUserId = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("mChatUser");

        mBottomPanel = new TelegramBottomPanel(this, this);
        //WhatsappViewCompat.applyFormatting(mBottomPanel.mInput);
        mBottomPanel.mInput.setMinLines(1);
        mBottomPanel.mInput.setMaxLines(20);
        mBottomPanel.mInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mBottomPanel.mInput.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mRefreshLayout = findViewById(R.id.message_refreshLayout);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        mChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setTitle(mChatUserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mTitleView = findViewById(R.id.custom_bar_name);
        mLastSeenView = findViewById(R.id.custom_bar_lastseen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        mTitleView.setText(mChatUserName);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();

            }
        });

        mRootRef.child("Users").child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")) {
                    mLastSeenView.setText("online");
                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText("last seen " + lastSeenTime);
                }
                Picasso.get().load(image).placeholder(R.drawable.profile_sample).into(mProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUserId)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUserId, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUserId + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT LOG ", databaseError.getMessage());
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
                imagePopup.initiatePopup(mProfileImage.getDrawable());
                imagePopup.viewPopup();

            }
        });

        mTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                profileIntent.putExtra("user_id", mChatUserId);
                startActivity(profileIntent);
            }
        });

    }

    @Override
    public void onAttachClicked() {

        CharSequence[] options = new CharSequence[]
                {
                        "Image",
                        "PDF",
                        "DOCX"
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose file type");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    fileType = "image";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), 0);
                }
                if (which == 1) {
                    fileType = "pdf";
                }
                if (which == 2) {
                    fileType = "docx";
                }
            }
        });
        builder.show();
    }

    //image selected
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //image picked
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUrl = data.getData();

            if (!fileType.equals("image")) {
                //not image

            } else if (fileType.equals("image")) {
                //image
                mProgressDialog.setTitle("Sending image");
                mProgressDialog.setMessage("Please wait while image is uploading...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("image files");

                final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUserId;
                final String chat_user_ref = "Messages/" + mChatUserId + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("Messages")
                        .child(mCurrentUserId)
                        .child(mChatUserId)
                        .push();
                final String push_id = user_message_push.getKey();

                final StorageReference filepath = storageReference.child(push_id + ".jpg");

                uploadtask = filepath.putFile(fileUrl);
                uploadtask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            Date date = new Date();
                            final String currentDateTime = dateFormat.format(date);

                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageMap = new HashMap();
                            messageImageMap.put("message", myUrl);
                            messageImageMap.put("seen", "false");
                            messageImageMap.put("type", fileType);
                            messageImageMap.put("time", currentDateTime);
                            messageImageMap.put("from", mCurrentUserId);
                            messageImageMap.put("to", mChatUserId);
                            messageImageMap.put("key", "null");
                            messageImageMap.put("message_id", push_id);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageImageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageImageMap);

                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        mProgressDialog.dismiss();
                                        Log.d("CHAT LOG ", databaseError.getMessage());
                                        Toast.makeText(ChatActivity.this, "Error uploading image!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

            } else {
                Toast.makeText(this, "Nothing seleted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error choosing file!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMicClicked() {
        Toast.makeText(this, "Mic Not Configured!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendClicked() {
        sendMessage();
    }
}
