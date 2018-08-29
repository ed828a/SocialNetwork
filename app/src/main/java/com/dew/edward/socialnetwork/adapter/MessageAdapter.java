package com.dew.edward.socialnetwork.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.model.Message;
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
 * Created by Edward on 8/29/2018.
 */
public class MessageAdapter extends FirebaseRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>{


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MessageAdapter(@NonNull FirebaseRecyclerOptions<Message> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MessageViewHolder holder, int position, @NonNull Message model) {
        String messageSenderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fromUserId = model.getFrom();
        String messageType = model.getType();

        DatabaseReference usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(fromUserId);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String imageUrl = dataSnapshot.child("profile_image").getValue().toString();
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(holder.receiverProfileImageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (messageType.equals("text")){

            if (fromUserId.equals(messageSenderId)){ // sender's ,messages
                holder.senderMessageTextView.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.senderMessageTextView.setTextColor(Color.WHITE);
                holder.senderMessageTextView.setGravity(Gravity.RIGHT);
                holder.senderMessageTextView.setText(model.getMessage());
                holder.senderMessageTextView.setVisibility(View.VISIBLE);
                holder.receiverMessageTextView.setVisibility(View.INVISIBLE);
                holder.receiverProfileImageView.setVisibility(View.INVISIBLE);
            } else {// receiver's messages
                holder.senderMessageTextView.setVisibility(View.INVISIBLE);
                holder.receiverMessageTextView.setVisibility(View.VISIBLE);
                holder.receiverProfileImageView.setVisibility(View.VISIBLE);
                holder.receiverMessageTextView.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.receiverMessageTextView.setTextColor(Color.WHITE);
                holder.receiverMessageTextView.setGravity(Gravity.LEFT);
                holder.receiverMessageTextView.setText(model.getMessage());
            }
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_users, parent, false);

        return new MessageViewHolder(view);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView senderMessageTextView, receiverMessageTextView;
        CircleImageView receiverProfileImageView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            receiverProfileImageView = itemView.findViewById(R.id.receiver_profile_image);
            receiverMessageTextView = itemView.findViewById(R.id.receiver_message_text);
            senderMessageTextView = itemView.findViewById(R.id.sender_message_text);
        }
    }
}
