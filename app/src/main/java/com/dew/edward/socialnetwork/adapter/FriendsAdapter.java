package com.dew.edward.socialnetwork.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.model.Friends;
import com.dew.edward.socialnetwork.ui.ChatActivity;
import com.dew.edward.socialnetwork.ui.FriendsActivity;
import com.dew.edward.socialnetwork.ui.PersonalProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Edward on 8/28/2018.
 */
public class FriendsAdapter extends FirebaseRecyclerAdapter<Friends, FriendsAdapter.FriendsViewHolder> {


    private DatabaseReference usersRef;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FriendsAdapter(@NonNull FirebaseRecyclerOptions options) {
        super(options);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
        holder.dateTextView.setText("Since: " + model.getDate());
        final String userId = getRef(position).getKey();

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String friendName = dataSnapshot.child("fullname").getValue().toString();
                    String friendProfileImage = dataSnapshot.child("profile_image").getValue().toString();
                    holder.friendNameTextView.setText(friendName);
                    Picasso.get().load(friendProfileImage).into(holder.friendProfileImageView);

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            CharSequence options[] = new CharSequence[]{
                                    friendName + "'s profile",
                                    "Send Message"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle("Select options");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    switch (i){
                                        case 0:
                                            sendUserToPersonalProfileActivity(view.getContext(), userId);
                                            break;
                                        case 1:
                                            sendUserToChatActivity(view.getContext(), friendName, userId);
                                            break;
                                    }
                                }
                            });
                            builder.create().show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToChatActivity(Context context, String username, String userId) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra("ListedUserId", userId);
        chatIntent.putExtra("userName", username);
        startActivity(context, chatIntent, null);
    }

    private void sendUserToPersonalProfileActivity(Context context, String userId) {

        Intent personalIntent = new Intent(context, PersonalProfileActivity.class);
        personalIntent.putExtra("ListedUserId", userId);
        startActivity(context, personalIntent, null);
    }


    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_users_display_layout, parent, false);
        return new FriendsViewHolder(view);
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView dateTextView;
        TextView friendNameTextView;
        CircleImageView friendProfileImageView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            dateTextView = itemView.findViewById(R.id.all_users_status);
            friendNameTextView = itemView.findViewById(R.id.all_users_profile_full_name);
            friendProfileImageView = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
