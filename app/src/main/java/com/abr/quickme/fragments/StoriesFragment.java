package com.abr.quickme.fragments;

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

import com.abr.quickme.R;
import com.abr.quickme.UploadNewStoryActivity;
import com.abr.quickme.models.Stories;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public class StoriesFragment extends Fragment {

    private View mMainView;

    private RecyclerView mStoryList;
    private FloatingActionButton addStoryButton;

    private DatabaseReference mStoryDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser;

    public StoriesFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_stories, container, false);
        mStoryList = mMainView.findViewById(R.id.story_list_recycler);

        mStoryList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).size(1).margin(250, 10).build());

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mStoryDatabase = FirebaseDatabase.getInstance().getReference().child("Stories");

        addStoryButton = mMainView.findViewById(R.id.new_story_button);
        addNewStory();

        mStoryList.setHasFixedSize(true);
        mStoryList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }

    //float button
    private void addNewStory() {
        addStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent storyIntent = new Intent(getContext(), UploadNewStoryActivity.class);
                storyIntent.putExtra("currentUser", mCurrentUser);
                startActivity(storyIntent);

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Stories> options =
                new FirebaseRecyclerOptions.Builder<Stories>()
                        .setQuery(mStoryDatabase, Stories.class)
                        .build();
        FirebaseRecyclerAdapter<Stories, StoriesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Stories, StoriesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(StoriesViewHolder storiesViewHolder, int position, Stories stories) {
                storiesViewHolder.setStatus(stories.getStoryText());
                storiesViewHolder.setImage(stories.getStoryImage());
                storiesViewHolder.setName(stories.getName());

            }

            @NonNull
            @Override
            public StoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_story_layout, parent, false);

                return new StoriesViewHolder(view);
            }
        };
        mStoryList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class StoriesViewHolder extends RecyclerView.ViewHolder {
        View mView;

        StoriesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        void setStatus(String storyText) {
            TextView storyTextView = mView.findViewById(R.id.story_text_D);
            storyTextView.setText(storyText);
        }

        void setName(String Name) {
            TextView status_name = mView.findViewById(R.id.status_name);
            status_name.setText(Name);
        }

        void setImage(String storyImage) {
            ImageView storyImageView = mView.findViewById(R.id.story_image_D);
            Picasso.get().load(storyImage).placeholder(R.drawable.profile_sample).into(storyImageView);
        }

    }
}
