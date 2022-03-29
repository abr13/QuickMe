package com.abr.quickme;

import static com.abr.quickme.ChatActivity.getAlphaNumericString;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abr.quickme.models.Messages;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scottyab.aescrypt.AESCrypt;
import com.squareup.picasso.Picasso;

import java.security.GeneralSecurityException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private final List<Messages> mMessageList;
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
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        holder.messageText.setVisibility(View.GONE);
        holder.timeText.setVisibility(View.GONE);
        holder.time_text_image.setVisibility(View.GONE);
        holder.messageImage.setVisibility(View.GONE);

        //show message image in fullscreen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMessageList.get(position).getType().equals("image")) {
                    final ImagePopup imagePopup = new ImagePopup(holder.itemView.getContext());
                    imagePopup.setWindowHeight(800); // Optional
                    imagePopup.setWindowWidth(800); // Optional
                    imagePopup.setBackgroundColor(Color.BLACK);  // Optional
                    imagePopup.setFullScreen(true); // Optional
                    imagePopup.setHideCloseIcon(true);  // Optional
                    imagePopup.setImageOnClickClose(true);// Optional
                    imagePopup.initiatePopup(holder.messageImage.getDrawable());
                    imagePopup.viewPopup();
                }
            }
        });

        final Messages message = mMessageList.get(position);
        final String from_user = message.getFrom();
        final String message_type = message.getType();

        if (message_type.equals("text")) {
            Toast.makeText(holder.itemView.getContext(), "here", Toast.LENGTH_LONG).show();
            holder.messageText.setVisibility(View.VISIBLE);
            holder.timeText.setVisibility(View.VISIBLE);
            //Decrypt Message Here
            String messageAfterDecrypt = "";
            try {
                messageAfterDecrypt = AESCrypt.decrypt(message.getKey(), message.getMessage());
                Log.d("here", "onBindViewHolder:decrypted2 " + message.getMessage() + "DD: " + messageAfterDecrypt);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
//alert notification required when app is in background
//            Alerter.create(holder.itemView.getContext())
//                    .setTitle("title")
//                    .setText("message")
//                    .show();


            holder.messageText.setText(messageAfterDecrypt);
            holder.timeText.setText(message.getTime());
        } else if (message_type.equals("image")) {
            holder.time_text_image.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.VISIBLE);

            Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_sample).into(holder.messageImage);
            holder.time_text_image.setText(message.getTime());
        }

        if (from_user.equals(Cuser.getUid())) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mMessageList.get(position).getType().equals("text")) {
                        CharSequence[] options = new CharSequence[]{"Edit message", "Delete for all", "Delete for me", "Cancel"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Chat option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //Edit for all
                                    editMessage(position, holder);
                                } else if (which == 1) {
                                    //Delete for all
                                    deleteMessageForAll(position, holder);
                                }
                                if (which == 2) {
                                    //Delete for me
                                    deleteSentMessage(position, holder);
                                }

                            }
                        });
                        builder.show();
                    } else if (mMessageList.get(position).getType().equals("image")) {
                        Toast.makeText(holder.itemView.getContext(), "Image", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });
        }

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

    private void deleteMessageForAll(final int position, final MessageViewHolder holder) {
        final DatabaseReference mRootRef;
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child("Messages")
                .child(mMessageList.get(position).getFrom())
                .child(mMessageList.get(position).getTo())
                .child(mMessageList.get(position).getMessage_id())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mRootRef.child("Messages")
                            .child(mMessageList.get(position).getTo())
                            .child(mMessageList.get(position).getFrom())
                            .child(mMessageList.get(position).getMessage_id())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(holder.itemView.getContext(), "Error deleting message!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error deleting message!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        final DatabaseReference mRootRef;
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child("Messages")
                .child(mMessageList.get(position).getFrom())
                .child(mMessageList.get(position).getTo())
                .child(mMessageList.get(position).getMessage_id())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error deleting message!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder holder) {
        final DatabaseReference mRootRef;
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child("Messages")
                .child(mMessageList.get(position).getTo())
                .child(mMessageList.get(position).getFrom())
                .child(mMessageList.get(position).getMessage_id())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error deleting message!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void editMessage(final int position, final MessageViewHolder holder) {

        String messageToEdit = mMessageList.get(position).getMessage();
        String keyOfeditMessage = mMessageList.get(position).getKey();

        String messageAfterDecryption = "";
        try {
            messageAfterDecryption = AESCrypt.decrypt(keyOfeditMessage, messageToEdit);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        Log.d("", "editMessage: " + messageAfterDecryption);

        AlertDialog.Builder alert = new AlertDialog.Builder(holder.itemView.getContext());

        alert.setTitle("Edit Message");
        alert.setMessage("Modify your message");

// Set an EditText view to get user input to edit message
        final EditText input = new EditText(holder.itemView.getContext());
        alert.setView(input);
        input.setPadding(30, 10, 30, 10);
        input.setText(messageAfterDecryption);

        alert.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
                final String messageAfterEdit = input.getText().toString() + "\n***EDITED***";

                final String key = getAlphaNumericString(20);
                String encryptedEditMessage = "";
                try {
                    encryptedEditMessage = AESCrypt.encrypt(key, messageAfterEdit);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                final DatabaseReference mRootRef;
                mRootRef = FirebaseDatabase.getInstance().getReference();
                final String finalEncryptedEditMessage = encryptedEditMessage;
                mRootRef.child("Messages")
                        .child(mMessageList.get(position).getFrom())
                        .child(mMessageList.get(position).getTo())
                        .child(mMessageList.get(position).getMessage_id())
                        .child("message").setValue(finalEncryptedEditMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mRootRef.child("Messages")
                                    .child(mMessageList.get(position).getFrom())
                                    .child(mMessageList.get(position).getTo())
                                    .child(mMessageList.get(position).getMessage_id())
                                    .child("key").setValue(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mRootRef.child("Messages")
                                                .child(mMessageList.get(position).getTo())
                                                .child(mMessageList.get(position).getFrom())
                                                .child(mMessageList.get(position).getMessage_id())
                                                .child("message").setValue(finalEncryptedEditMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mRootRef.child("Messages")
                                                            .child(mMessageList.get(position).getTo())
                                                            .child(mMessageList.get(position).getFrom())
                                                            .child(mMessageList.get(position).getMessage_id())
                                                            .child("key").setValue(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                MessageAdapter m = new MessageAdapter(mMessageList);
                                                                m.notifyDataSetChanged();

                                                                Toast.makeText(holder.itemView.getContext(), "Please get back again to see edited message!", Toast.LENGTH_LONG).show();

                                                            } else {
                                                                Toast.makeText(holder.itemView.getContext(), "Error editing!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });

            }
        });

        alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    public void alert(String title, String message) {

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, time_text_image;
        ImageView messageImage;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);

            messageImage = itemView.findViewById(R.id.message_image);
            time_text_image = itemView.findViewById(R.id.time_text_image);
        }

    }
}
