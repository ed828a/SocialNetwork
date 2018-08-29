package com.dew.edward.socialnetwork.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.model.Comment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;




/**
 * Created by Edward on 8/26/2018.
 */
public class CommentAdapter extends FirebaseRecyclerAdapter<Comment, CommentAdapter.CommentViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CommentAdapter(@NonNull FirebaseRecyclerOptions<Comment> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {

        holder.usernameTextView.setText(model.getUsername());
        holder.commentContentTextView.setText(model.getComment());
        holder.commentDateTextView.setText(model.getDate());
        holder.commentTimeTextView.setText(model.getTime());
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_comment_layout, parent, false);

        return new CommentViewHolder(view);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder{
        TextView usernameTextView, commentDateTextView, commentContentTextView, commentTimeTextView;
        public CommentViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.comment_username);
            commentDateTextView = itemView.findViewById(R.id.comment_date);
            commentContentTextView = itemView.findViewById(R.id.comment_contents);
            commentTimeTextView = itemView.findViewById(R.id.comment_time);
        }
    }
}
