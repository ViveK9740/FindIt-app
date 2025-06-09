package com.findit.app;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<FeedItem> feedItems;


    public FeedAdapter(Context context,List<FeedItem> feedItems) {
        this.context = context;
        this.feedItems = feedItems;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = feedItems.get(position);

        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_pin) // optional
                    .error(R.drawable.ic_pin)             // optional
                    .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE); // hide if no image
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(item.getTime()));
        holder.timeTextView.setText(formattedTime);

        holder.postedBy.setText(item.getUsername());

        String userId = item.getUserId();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (item.getUserId().equals(currentUserId)) {
            holder.chatButton.setVisibility(View.GONE);  // Hide button if it's your post
        } else {
            holder.chatButton.setVisibility(View.VISIBLE);
            holder.chatButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiverId", item.getUserId());
                intent.putExtra("chatUserName", item.getUsername());
                context.startActivity(intent);
            });
        }

    }


    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, timeTextView, descriptionTextView , postedBy;
        ImageView imageView;
        Button chatButton;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            postedBy = itemView.findViewById(R.id.postedBy);
            titleTextView = itemView.findViewById(R.id.itemTitle);
            timeTextView = itemView.findViewById(R.id.itemTime);
            descriptionTextView = itemView.findViewById(R.id.itemDescription);
            imageView = itemView.findViewById(R.id.itemImage);
            chatButton = itemView.findViewById(R.id.chatBtn);
        }
    }
}
