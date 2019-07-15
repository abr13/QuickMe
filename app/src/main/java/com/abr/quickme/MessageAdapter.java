package com.abr.quickme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private List<Messages> mMessageList;
    private FirebaseUser Cuser;


    MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_right, parent, false);
            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_left, parent, false);
            return new MessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Messages message = mMessageList.get(position);
        //String from_user = message.getFrom();
        holder.messageText.setText(message.getMessage());
        holder.timeText.setText(message.getTime());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {

        Cuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessageList.get(position).getFrom().equals(Cuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }
    }
}
