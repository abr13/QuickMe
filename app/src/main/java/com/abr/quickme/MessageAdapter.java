package com.abr.quickme;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        mAuth = FirebaseAuth.getInstance();

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages m = mMessageList.get(position);
        String from_user = m.getFrom();
        holder.messageText.setText(m.getMessage());
        //holder.timeText.setText(m.getTime());

        if (from_user.equals(current_user_id)) {
            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);
        } else {
            holder.messageText.setBackgroundColor(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
        }

        //Picasso.get().load(thumb).placeholder(R.drawable.profile_sample).into(holder.message_profile_image);

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, timeText;
        public CircleImageView message_profile_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            //message_profile_image=itemView.findViewById(R.id.message_profile_image);

        }
    }
}
