package com.dew.edward.socialnetwork.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dew.edward.socialnetwork.ui.ClickPostActivity;
import com.dew.edward.socialnetwork.model.Post;
import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.ui.CommentsActivity;
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

import static android.support.v4.content.ContextCompat.startActivities;
import static android.support.v4.content.ContextCompat.startActivity;


/**
 * Created by Edward on 8/24/2018.
 */
public class PostAdapter extends FirebaseRecyclerAdapter<Post, PostAdapter.PostViewHolder> {


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public PostAdapter(@NonNull FirebaseRecyclerOptions<Post> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull Post model) {
        final String postKey = getRef(position).getKey();  // postKey is the post id, under postKey(postId) are userIds

        holder.setDate(model.getDate());
        holder.setTime(model.getTime());
        holder.setDescription(model.getDescription());
        holder.setFull_name(model.getFull_name());
        holder.setPost_image(model.getPost_image());
        holder.setProfile_image(model.getProfile_image());
        holder.setLikeButtonStatus(postKey);
        Log.d("Main", model.getFull_name());


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToMainActivity(view.getContext(), postKey);
            }
        });



        holder.likePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.likeChecker = true;
                holder.likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (holder.likeChecker) {
                            if (dataSnapshot.child(postKey).hasChild(holder.currentUserId)) {
                                holder.likesRef.child(postKey).child(holder.currentUserId).removeValue();
                                holder.likeChecker = false;

                            } else {
                                holder.likesRef.child(postKey).child(holder.currentUserId).setValue(true);
                                holder.likeChecker = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToCommentsActivity(view.getContext(), postKey);
            }
        });
    }

    private void sendUserToCommentsActivity(Context context, String postKey) {
        Intent commentsIntent = new Intent(context, CommentsActivity.class);
        commentsIntent.putExtra("PostKey", postKey);
        startActivity(context, commentsIntent, null);
    }

    private void sendUserToMainActivity(Context context, String postKey) {
        Intent clickPostIntent = new Intent(context, ClickPostActivity.class);
        clickPostIntent.putExtra("PostKey", postKey);
        startActivity(context, clickPostIntent, null);
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_posts_layout, parent, false);

        return new PostViewHolder(view);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;
        boolean likeChecker = false;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            likePostButton = mView.findViewById(R.id.like_button);
            commentPostButton = mView.findViewById(R.id.comment_button);
            displayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }


        public void setFull_name(String full_name) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(full_name);
        }

        public void setProfile_image(String profile_image) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profile_image).into(image);
        }

        public void setTime(String time) {
            TextView timeTextView = (TextView) mView.findViewById(R.id.post_time);
            timeTextView.setText("   " + time);
        }

        public void setDate(String date) {
            TextView dateTextView = (TextView) mView.findViewById(R.id.post_date);
            dateTextView.setText("   " + date);
        }

        public void setDescription(String description) {
            TextView descriptionTextView = (TextView) mView.findViewById(R.id.detail_post_description);
            descriptionTextView.setText(description);
        }

        public void setPost_image(String post_image) {
            ImageView image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(post_image).into(image);
        }

        // show likes
        public void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText(Integer.toString(countLikes) + " likes");
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText(Integer.toString(countLikes) + " likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}

