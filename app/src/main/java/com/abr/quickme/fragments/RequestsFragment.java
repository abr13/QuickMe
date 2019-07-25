package com.abr.quickme.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.R;
import com.abr.quickme.models.Requests;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = "REQUEST";
    Button reqAcceptBtn, reqDeclineBtn;
    String listUserId;
    private RecyclerView mFriendReqsList;
    private DatabaseReference mFriendReqDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;
    private DatabaseReference mFriendRequestDatabase, mFriendDatabase, getTypeRef;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        mFriendReqsList = mMainView.findViewById(R.id.requests_list_recycler);


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
        final FirebaseRecyclerAdapter<Requests, RequestsFragment.requestsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Requests, RequestsFragment.requestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final requestsViewHolder requestsViewHolder, final int i, @NonNull Requests requests) {
                        listUserId = getRef(i).getKey();
                        getTypeRef = getRef(i).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onDataChange: TEST" + i);
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received")) {
                                        mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String name = dataSnapshot.child("name").getValue().toString();
                                                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                                requestsViewHolder.setName(name);
                                                requestsViewHolder.setThumb_image(thumb_image);
                                                requestsViewHolder.setReqType("Request Received");

                                                reqAcceptBtn = requestsViewHolder.itemView.findViewById(R.id.req_accept_btn);
                                                reqAcceptBtn.setText("Accept");
                                                reqDeclineBtn = requestsViewHolder.itemView.findViewById(R.id.req_decline_btn);
                                                reqDeclineBtn.setText("Decline");

                                                //Accept request
                                                reqAcceptBtn.setOnClickListener(new View.OnClickListener() {
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
                                                reqDeclineBtn.setOnClickListener(new View.OnClickListener() {
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
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String name = dataSnapshot.child("name").getValue().toString();
                                                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                                requestsViewHolder.setName(name);
                                                requestsViewHolder.setThumb_image(thumb_image);
                                                requestsViewHolder.setReqType("Request Sent");

                                                requestsViewHolder.itemView.findViewById(R.id.req_accept_btn).setVisibility(View.INVISIBLE);
                                                reqDeclineBtn = requestsViewHolder.itemView.findViewById(R.id.req_decline_btn);
                                                reqDeclineBtn.setText("Cancel Request ");

                                                reqDeclineBtn.setOnClickListener(new View.OnClickListener() {
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
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsFragment.requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.single_request_layout, parent, false);

                        return new RequestsFragment.requestsViewHolder(view);
                    }

                };

        mFriendReqsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {

    }

    public class requestsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        requestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.req_single_name);
            userNameView.setText(name);
        }

        void setThumb_image(String thumb_image) {
            CircleImageView imageView = mView.findViewById(R.id.req_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile_sample).into(imageView);
        }

        void setReqType(String requestType) {
            TextView reqType = mView.findViewById(R.id.req_type);
            reqType.setText(requestType);
        }
    }
}