package com.dew.edward.socialnetwork.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.model.FindFriends;
import com.dew.edward.socialnetwork.ui.FindFriendsActivity;
import com.dew.edward.socialnetwork.ui.PersonalProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.v4.content.ContextCompat.startActivity;


/**
 * Created by Edward on 8/25/2018.
 */
public class FindFriendsAdapter extends FirebaseRecyclerAdapter<FindFriends, FindFriendsAdapter.FindFriendsViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FindFriendsAdapter(@NonNull FirebaseRecyclerOptions<FindFriends> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull final FindFriends model) {
        Picasso.get().load(model.getProfile_image()).into(holder.profileImage);
        holder.profileFullname.setText(model.getFullname());
        holder.profileStatus.setText(model.getRelationshipstatus());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String listedUserId = getRef(position).getKey(); // listed user's userId
                sendUserToPersonalProfileActivity(view.getContext(), listedUserId);
            }
        });
    }

    private void sendUserToPersonalProfileActivity(Context context, String listedUserId) {
        Intent personalIntent = new Intent(context, PersonalProfileActivity.class);
        personalIntent.putExtra("ListedUserId", listedUserId);
        startActivity(context, personalIntent, null);
    }

    @NonNull
    @Override
    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_users_display_layout, parent, false);

        return new FindFriendsViewHolder(view);
    }

    class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView profileFullname;
        TextView profileStatus;
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            profileImage = itemView.findViewById(R.id.all_users_profile_image);
            profileFullname = itemView.findViewById(R.id.all_users_profile_full_name);
            profileStatus = itemView.findViewById(R.id.all_users_status);

        }

    }
}
