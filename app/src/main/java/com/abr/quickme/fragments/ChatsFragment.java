package com.abr.quickme.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.ChatActivity;
import com.abr.quickme.GetTimeAgo;
import com.abr.quickme.R;
import com.abr.quickme.models.Chats;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mChatsList;

    private DatabaseReference mChatsDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        mChatsList = mMainView.findViewById(R.id.chats_list_recycler);

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

                TextView layout_single_status = view.findViewById(R.id.layout_single_status);
                layout_single_status.setEnabled(false);
                layout_single_status.setVisibility(View.INVISIBLE);

                return new ChatsFragment.chatsViewHolder(view);
            }
        };
        mChatsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class chatsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        chatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

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

                String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, context);

                userOnlineView.setVisibility(View.INVISIBLE);
                userLastseen.setVisibility(View.VISIBLE);
                userLastseen.setText("last seen : " + lastSeenTime);
            }
        }
    }
}
