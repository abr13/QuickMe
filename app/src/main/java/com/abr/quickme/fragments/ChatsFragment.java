package com.abr.quickme.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.ChatActivity;
import com.abr.quickme.R;
import com.abr.quickme.classes.GetTimeAgo;
import com.abr.quickme.models.Chats;
import com.abr.quickme.models.Messages;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mChatsList;

    private DatabaseReference mChatsDatabase, mUsersDatabase, mFriendDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    private String thelastMessage;
    private DatabaseReference mUserDatabase;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //online


        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        mChatsList = mMainView.findViewById(R.id.chats_list_recycler);

        mChatsList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).size(1).margin(250, 10).build());

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mChatsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Chats> options =
                new FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(mChatsDatabase, Chats.class)
                        .build();
        FirebaseRecyclerAdapter<Chats, ChatsFragment.chatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsFragment.chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder chatsViewHolder, int i, @NonNull Chats chats) {

                final String listUserId = getRef(i).getKey();

                //last message
//                chatsViewHolder.layout_last_message.setEnabled(true);
//                chatsViewHolder.layout_last_message.setVisibility(View.VISIBLE);
//                lastMessage(listUserId, chatsViewHolder.layout_last_message);

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();


                        chatsViewHolder.setName(name);
                        chatsViewHolder.setThumb(thumb_image);

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            chatsViewHolder.setUserOnline(userOnline, getContext());
                        }

                        chatsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", listUserId);
                                chatIntent.putExtra("mChatUser", name);
                                startActivity(chatIntent);
                            }
                        });
                        chatsViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                CharSequence[] options = new CharSequence[]{"Delete Chat with Messages\nyou can't revert it back."};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Chat option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            final DatabaseReference mRootRef;
                                            mRootRef = FirebaseDatabase.getInstance().getReference();
                                            mRootRef.child("Messages").child(mCurrentUserId).child(listUserId).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mRootRef.child("Messages").child(listUserId).child(mCurrentUserId).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mRootRef.child("Chat").child(listUserId).child(mCurrentUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    mRootRef.child("Chat").child(mCurrentUserId).child(listUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            Toast.makeText(getContext(), "All Messages and Chat Deleted!", Toast.LENGTH_SHORT).show();
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
                                });
                                builder.show();

                                return true;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsFragment.chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new ChatsFragment.chatsViewHolder(view);
            }
        };
        mChatsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //last message
    private void lastMessage(final String userId, final TextView last_msg) {
        thelastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Messages messages = snapshot.getValue(Messages.class);
                    if (messages != null) {
                        if (messages.getTo().equals(firebaseUser.getUid()) && messages.getFrom().equals(userId)
                                || messages.getFrom().equals(firebaseUser.getUid()) && messages.getTo().equals(userId)) {
                            thelastMessage = messages.getMessage();
                        }
                    }
                }
                switch (thelastMessage) {
                    case "default":
                        last_msg.setText("");
                        break;
                    default:
                        last_msg.setText(thelastMessage);
                        break;
                }
                thelastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class chatsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView layout_last_message;

        chatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            layout_last_message = mView.findViewById(R.id.layout_single_status);
            layout_last_message.setEnabled(false);
            layout_last_message.setVisibility(View.INVISIBLE);

        }

        void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.layout_single_name);
            userNameView.setText(name);
        }

        void setThumb(String thumb_image) {
            CircleImageView imageView = mView.findViewById(R.id.layout_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile_sample).into(imageView);
        }

        void setUserOnline(String online_status, Context context) {
            ImageView userOnlineView = mView.findViewById(R.id.layout_single_isOnline);
            TextView userLastseen = mView.findViewById(R.id.layout_single_lastseen);
            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
                userLastseen.setVisibility(View.INVISIBLE);
            } else {
                GetTimeAgo getTimeAgo = new GetTimeAgo();

                long lastTime = Long.parseLong(online_status);

                String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime - 1000, context);

                userOnlineView.setVisibility(View.INVISIBLE);
                userLastseen.setVisibility(View.VISIBLE);
                userLastseen.setText("last seen : " + lastSeenTime);
            }
        }


    }
}
