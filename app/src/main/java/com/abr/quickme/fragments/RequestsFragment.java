package com.abr.quickme.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.R;
import com.abr.quickme.models.Requests;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private static final String TAG = "REQUEST";
    private RecyclerView mFriendReqsList;
    private DatabaseReference mFriendReqDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;
    private DatabaseReference mFriendRequestDatabase, mFriendDatabase, getTypeRef;
    private String image;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        mFriendReqsList = mMainView.findViewById(R.id.requests_list_recycler);

        mFriendReqsList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).size(1).margin(200, 150).build());

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(mCurrentUserId);
//        mFriendReqDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
//        mUsersDatabase.keepSynced(true);

        mFriendReqsList.setHasFixedSize(true);
        mFriendReqsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(mFriendReqDatabase, Requests.class)
                        .build();

        FirebaseRecyclerAdapter<Requests, requestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, requestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final requestsViewHolder holder, int position, @NonNull Requests model) {
                        holder.reqAcceptBtn.setVisibility(View.VISIBLE);
                        holder.reqDeclineBtn.setVisibility(View.VISIBLE);

                        final ImagePopup imagePopup = new ImagePopup(holder.itemView.getContext());
                        imagePopup.setWindowHeight(800); // Optional
                        imagePopup.setWindowWidth(800); // Optional
                        imagePopup.setBackgroundColor(Color.BLACK);  // Optional
                        imagePopup.setFullScreen(true); // Optional
                        imagePopup.setHideCloseIcon(true);  // Optional
                        imagePopup.setImageOnClickClose(true);// Optional


                        final String listUserId = getRef(position).getKey(); // Gets Chatrequest / userID / first item.

                        //user clicks in profile image, show full image popup
                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mUsersDatabase.child(listUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        image = dataSnapshot.getValue().toString();

                                        imagePopup.initiatePopup(holder.imageView.getDrawable());
                                        imagePopup.viewPopup();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                        final DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")) {
                                        mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String name = dataSnapshot.child("name").getValue().toString();
                                                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                                Picasso.get().load(thumb_image).placeholder(R.drawable.profile_sample).into(holder.imageView);
                                                holder.userNameView.setText(name);
                                                holder.reqType.setText("Request received");

                                                holder.reqAcceptBtn.setText("Accept");
                                                holder.reqDeclineBtn.setText("Decline");

                                                //Accept request
                                                holder.reqAcceptBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                        Date date = new Date();
                                                        final String currentDateTime = dateFormat.format(date);

                                                        mFriendDatabase.child(mCurrentUserId).child(listUserId).child("date").setValue("Friend since :" + " " + currentDateTime)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        mFriendDatabase.child(listUserId).child(mCurrentUserId).child("date").setValue("Friend since :" + " " + currentDateTime)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {

                                                                                        mFriendRequestDatabase.child(mCurrentUserId).child(listUserId).removeValue()
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        mFriendRequestDatabase.child(listUserId).child(mCurrentUserId).removeValue()
                                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Void aVoid) {

                                                                                                                        Toast.makeText(getContext(), "Hohoo, Accepted, Party Started ", Toast.LENGTH_LONG).show();
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });

                                                //Decline Request
                                                holder.reqDeclineBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mFriendRequestDatabase.child(mCurrentUserId).child(listUserId).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            mFriendRequestDatabase.child(listUserId).child(mCurrentUserId).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                Toast.makeText(getContext(), "Request Declined, you're rud! ", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String name = dataSnapshot.child("name").getValue().toString();
                                                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                                Picasso.get().load(thumb_image).placeholder(R.drawable.profile_sample).into(holder.imageView);
                                                holder.userNameView.setText(name);
                                                holder.reqType.setText("Request sent");

                                                holder.reqAcceptBtn.setVisibility(View.INVISIBLE);
                                                holder.reqDeclineBtn.setText("Cancel");

                                                holder.reqDeclineBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mFriendRequestDatabase.child(mCurrentUserId).child(listUserId).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mFriendRequestDatabase.child(listUserId).child(mCurrentUserId).removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Toast.makeText(getContext(), "Request Cancelled, you're rud! ", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_request_layout, viewGroup, false);
                        requestsViewHolder holder = new requestsViewHolder(view);
                        return holder;
                    }
                };
        mFriendReqsList.setAdapter(adapter);
        adapter.startListening();
    }


    public class requestsViewHolder extends RecyclerView.ViewHolder {

        TextView userNameView, reqType;
        CircleImageView imageView;
        MaterialButton reqAcceptBtn, reqDeclineBtn;

        public requestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameView = itemView.findViewById(R.id.req_single_name);
            imageView = itemView.findViewById(R.id.req_single_image);
            reqType = itemView.findViewById(R.id.req_type);
            reqAcceptBtn = itemView.findViewById(R.id.req_accept_btn);
            reqDeclineBtn = itemView.findViewById(R.id.req_decline_btn);
        }
    }
}