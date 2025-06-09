package com.findit.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    TextView usernameTextView, emailTextView;
    RecyclerView myPostsRecyclerView;
    FeedAdapter feedAdapter;
    List<FeedItem> myPostsList = new ArrayList<>();
    String currentUserId, username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // UI references
        usernameTextView = findViewById(R.id.profileUsername);
        emailTextView = findViewById(R.id.profileEmail);
        myPostsRecyclerView = findViewById(R.id.myPostsRecyclerView);

        // Get user details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("uid", null);
        username = sharedPreferences.getString("username", "Unknown");
        email = sharedPreferences.getString("email", "Unknown");

        usernameTextView.setText("Username: " + username);
        emailTextView.setText("Email: " + email);

        // Set up RecyclerView
        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, myPostsList);
        myPostsRecyclerView.setAdapter(feedAdapter);

        // Fetch posts made by this user
        fetchMyPosts();
    }

    private void fetchMyPosts() {
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("items")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myPostsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FeedItem item = new FeedItem(
                                doc.getString("userId"),
                                doc.getString("username") != null ? doc.getString("username") : "Unknown",
                                doc.getString("title"),
                                doc.getLong("time"),
                                doc.getString("description"),
                                doc.getString("imgUrl") // Default image or adjust as needed
                        );
                        myPostsList.add(item);
                    }
                    feedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
                );
    }
}
