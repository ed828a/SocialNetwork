package com.dew.edward.socialnetwork.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.adapter.MessageAdapter;
import com.dew.edward.socialnetwork.model.Message;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendTextMessageButton, sendImageMessageButton;
    private EditText messageInput;
    private RecyclerView userMessageList;
    private MessageAdapter adapter;

    private String messageReceiverId, messageReceiverName, messageSenderId;
    private TextView receiverNameView;
    private CircleImageView receiverProfileImageView;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        messageSenderId = mAuth.getCurrentUser().getUid();
        messageReceiverId = getIntent().getStringExtra("ListedUserId");
        messageReceiverName = getIntent().getStringExtra("userName");


        initializeFields();
        displayReceiverInfo();
        sendTextMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTextMessage();
            }
        });
    }

    private void sendTextMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please type a message ...", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderPath = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverPath = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference messageSenderRef = rootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();
            String message_push_id = messageSenderRef.getKey();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-mm-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calFordDate.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            Map messageBodies = new HashMap();
            messageBodies.put(messageSenderPath + "/" + message_push_id, messageTextBody);
            messageBodies.put(messageReceiverPath + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodies).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this,
                                "message sent successfully", Toast.LENGTH_SHORT).show();
                        messageInput.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this,
                                "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void displayReceiverInfo() {
        receiverNameView.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String profileImage = dataSnapshot.child("profile_image").getValue().toString();
                            Picasso.get().load(profileImage).into(receiverProfileImageView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initializeFields() {

        mToolbar = findViewById(R.id.chat_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        getSupportActionBar().setCustomView(action_bar_view);

        sendTextMessageButton = findViewById(R.id.messages_send_message);
        sendImageMessageButton = findViewById(R.id.messages_select_image);
        messageInput = findViewById(R.id.messages_input);
        userMessageList = findViewById(R.id.messages_list_users);

        receiverNameView = findViewById(R.id.custom_profile_name);
        receiverProfileImageView = findViewById(R.id.custom_profile_image);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setHasFixedSize(true);

        DatabaseReference senderMessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(messageSenderId);
        Query messageQuery;
        messageQuery = senderMessagesRef.child(messageReceiverId);
        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(messageQuery, Message.class)
                .build();
        adapter = new MessageAdapter(options);
        userMessageList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
