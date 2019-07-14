package com.abr.quickme;


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
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendsDatabase, Friends.class)
                        .build();
        FirebaseRecyclerAdapter<Friends, friendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {

                friendsViewHolder.setDate(friends.getDate());

                String listUserId = getRef(i).getKey();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        friendsViewHolder.setName(name);
                        friendsViewHolder.setThumb_image(thumb_image);

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new friendsViewHolder(view);
            }
        };
        mFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        friendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        void setDate(String date) {
            TextView userStatusView = mView.findViewById(R.id.layout_single_status);
            userStatusView.setText(date);
        }

        void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.layout_single_name);
            userNameView.setText(name);
        }

        void setThumb_image(String thumb_image) {
            CircleImageView imageView = mView.findViewById(R.id.layout_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile_sample).into(imageView);
        }

        void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.layout_single_isOnline);
            TextView userLastseen = mView.findViewById(R.id.layout_single_lastseen);
            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
                userLastseen.setVisibility(View.INVISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
                userLastseen.setVisibility(View.VISIBLE);
                userLastseen.setText("Lastseen : " + online_status);
            }
        }
    }
}
