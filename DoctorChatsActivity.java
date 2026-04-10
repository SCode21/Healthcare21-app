package com.example.signuploginfirebasee;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
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

public class DoctorChatsActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private FirestoreRecyclerAdapter<ChatMessage, ChatActivity.ChatViewHolder> adapter;
    private FirebaseFirestore db;
    private String doctorId, patientId;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_chats);

        db = FirebaseFirestore.getInstance();
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        chatRecyclerView.setItemAnimator(null);

        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());

        doctorId = getIntent().getStringExtra("doctorId");
        patientId = getIntent().getStringExtra("patientId");
        chatRoomId = ChatUtils.getChatRoomId(doctorId, patientId);

        setupChatAdapter();
    }

    private void setupChatAdapter() {
        Query query = db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChatMessage, ChatActivity.ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatActivity.ChatViewHolder holder, int position, @NonNull ChatMessage model) {
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
            public ChatActivity.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_message, parent, false);
                return new ChatActivity.ChatViewHolder(view);
            }
        };

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
    }

    private void setMessageAppearance(ChatActivity.ChatViewHolder holder, boolean isCurrentUser) {
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
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ChatMessage chatMessage = new ChatMessage(message, senderId);

            db.collection("chats")
                    .document(chatRoomId)
                    .collection("messages")
                    .add(chatMessage)
                    .addOnSuccessListener(documentReference -> {
                        inputMessage.setText("");
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
}