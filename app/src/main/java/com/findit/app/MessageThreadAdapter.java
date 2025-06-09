package com.findit.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageThreadAdapter extends RecyclerView.Adapter<MessageThreadAdapter.ViewHolder> {

    private List<ChatThread> chatThreads;
    private Context context;

    public MessageThreadAdapter(List<ChatThread> chatThreads, Context context) {
        this.chatThreads = chatThreads;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameView, lastMessageView;

        public ViewHolder(View view) {
            super(view);
            usernameView = view.findViewById(R.id.usernameTextView);
            lastMessageView = view.findViewById(R.id.lastMessageTextView);
        }
    }

    @NonNull
    @Override
    public MessageThreadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_thread_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageThreadAdapter.ViewHolder holder, int position) {
        ChatThread chat = chatThreads.get(position);
        holder.usernameView.setText(chat.getUsername());
        holder.lastMessageView.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");

        Log.d("Adapter", "Binding: " + chat.getUsername() + " – " + chat.getLastMessage());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", chat.getUserId());
            intent.putExtra("chatUserName", chat.getUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatThreads.size();
    }
}
