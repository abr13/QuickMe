package com.abr.quickme.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.CallingActivity;
import com.abr.quickme.R;
import com.abr.quickme.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public class VideoListFragment extends Fragment {

    private RecyclerView mVideoList;
    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;
    private String calledBy = "";

    private View mMainView;


    public VideoListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        checkForReceivingCall();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions
                        .Builder<Friends>()
                        .setQuery(mFriendsDatabase, Friends.class)
                        .build();
        FirebaseRecyclerAdapter<Friends, FriendsFragment.friendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsFragment.friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsFragment.friendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {

                friendsViewHolder.setDate(friends.getDate());

                final String listUserId = getRef(i).getKey();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        friendsViewHolder.setName(name);
                        friendsViewHolder.setThumb_image(thumb_image);

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline, getContext());
                        }

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence[] options = new CharSequence[]{"Make Video Call", "Open Group Call"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Click to Continue...");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            Toast.makeText(getContext(), "Under development!", Toast.LENGTH_SHORT).show();
//                                            Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
//                                            Intent videocallIntent = new Intent(getContext(), CallingActivity.class);////////////////////
//                                            videocallIntent.putExtra("user_id", listUserId);
//                                            startActivity(videocallIntent);
                                        }
                                        if (which == 1) {
                                            Toast.makeText(getContext(), "Opening Group Video Chat", Toast.LENGTH_SHORT).show();
//                                            Intent videocallIntent = new Intent(getContext(), WebActivity.class);////////////////////
//                                            videocallIntent.putExtra("user_id", listUserId);
//                                            startActivity(videocallIntent);

                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demos.openvidu.io/openvidu-call/#/z1y2x3"));
                                            startActivity(browserIntent);
                                        }
                                    }
                                });
                                builder.show();
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
            public FriendsFragment.friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new FriendsFragment.friendsViewHolder(view);
            }
        };
        mFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall() {
        mUsersDatabase.child(mCurrentUserId).child("ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ringing")) {
                    calledBy = dataSnapshot.child("ringing").getValue().toString();

                    Intent callIntent = new Intent(getActivity(), CallingActivity.class);//////////////////
                    callIntent.putExtra("user_id", calledBy);//
                    startActivity(callIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_video_list, container, false);
        mFriendsList = mMainView.findViewById(R.id.videochat_list_recycler);

        mFriendsList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).size(1).margin(250, 10).build());

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
}
