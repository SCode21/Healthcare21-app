package com.example.signuploginfirebasee;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Calendar;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder> adapter;
    private FirebaseFirestore db;
    private String patientId, doctorId;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            db = FirebaseFirestore.getInstance();
            chatRecyclerView = findViewById(R.id.chatRecyclerView);
            messageEditText = findViewById(R.id.messageEditText);
            chatRecyclerView.setItemAnimator(null);

            findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());

            Intent intent = getIntent();
            if (intent != null) {
                doctorId = intent.getStringExtra("doctorId");
                patientId = intent.getStringExtra("patientId");

                if (doctorId == null || patientId == null) {
                    Toast.makeText(this, "Missing IDs", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                chatRoomId = ChatUtils.getChatRoomId(patientId, doctorId);
                setupChatAdapter();
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "Init error", e);
            finish();
        }
    }

    private void setupChatAdapter() {
        Query query = db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull ChatMessage model) {
                holder.messageText.setText(model.getMessage());

                if (model.getTimestamp() != null) {
                    String time = DateFormat.format("h:mm a", model.getTimestamp().toDate()).toString();
                    holder.messageTime.setText(time);
                }

                // Use current user ID for comparison
                boolean isCurrentUser = model.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setMessageAppearance(holder, isCurrentUser);
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_message, parent, false);
                return new ChatViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (getItemCount() > 0) {
                    chatRecyclerView.smoothScrollToPosition(getItemCount() - 1);
                }
            }
        };

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
    }

    private void setMessageAppearance(ChatViewHolder holder, boolean isCurrentUser) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        int paddingSmall = (int) (8 * getResources().getDisplayMetrics().density);

        if (isCurrentUser) {
            // Current user's messages (green bubble on right)
            params.gravity = Gravity.END;
            holder.messageText.setBackgroundResource(R.drawable.whatsapp_sent_bubble);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageText.setPadding(padding, paddingSmall, paddingSmall, padding);
            holder.messageTime.setGravity(Gravity.END);
        } else {
            // Other user's messages (white bubble on left)
            params.gravity = Gravity.START;
            holder.messageText.setBackgroundResource(R.drawable.whatsapp_received_bubble);
            holder.messageText.setTextColor(Color.BLACK);
            holder.messageText.setPadding(paddingSmall, paddingSmall, padding, padding);
            holder.messageTime.setGravity(Gravity.START);
        }
        holder.messageContainer.setLayoutParams(params);
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ChatMessage chatMessage = new ChatMessage(message, senderId);

            db.collection("chats")
                    .document(chatRoomId)
                    .collection("messages")
                    .add(chatMessage)
                    .addOnSuccessListener(documentReference -> {
                        messageEditText.setText("");
                    });
        }
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

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        FrameLayout messageContainer;

        public ChatViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextView);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
}