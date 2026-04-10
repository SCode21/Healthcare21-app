package com.example.signuploginfirebasee;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private Context context;
    private List<ChatMessage> chatMessages;
    private String userId; // ID of the current user (patient or doctor)

    public ChatAdapter(Context context, List<ChatMessage> chatMessages, String userId) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each chat message item
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // Get the chat message at the current position
        ChatMessage chatMessage = chatMessages.get(position);

        // Set the message text
        holder.messageTextView.setText(chatMessage.getMessage());

        // Align the message based on the sender
        if (chatMessage.getSenderId().equals(userId)) {
            // If the message is sent by the current user, align it to the right
            holder.messageTextView.setGravity(Gravity.END);
            holder.messageTextView.setBackgroundResource(R.drawable.whatsapp_sent_bubble);
        } else {
            // If the message is received, align it to the left
            holder.messageTextView.setGravity(Gravity.START);
            holder.messageTextView.setBackgroundResource(R.drawable.whatsapp_received_bubble);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder class to hold the views for each chat message item
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}