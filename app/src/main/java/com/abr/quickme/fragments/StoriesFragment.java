package com.abr.quickme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.R;
import com.abr.quickme.UploadNewStatusActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StoriesFragment extends Fragment {

    private View mMainView;

    private RecyclerView mStoryList;
    private FloatingActionButton addStoryButton;

    private DatabaseReference mStoryDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser;

    public StoriesFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_stories, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        mStoryDatabase = FirebaseDatabase.getInstance().getReference().child("Stories").child(mCurrentUser);

        addStoryButton = mMainView.findViewById(R.id.new_story_button);
        addNewStory();

        return mMainView;

    }

    private void addNewStory() {
        addStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent storyIntent = new Intent(getContext(), UploadNewStatusActivity.class);
                storyIntent.putExtra("currentUser", mCurrentUser);
                startActivity(storyIntent);

            }
        });

    }

}
