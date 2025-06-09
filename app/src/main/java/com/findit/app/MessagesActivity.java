package com.findit.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageThreadAdapter adapter;
    private List<ChatThread> chatThreads = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyView;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerView = findViewById(R.id.messagesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageThreadAdapter(chatThreads, this);
        recyclerView.setAdapter(adapter);

        loadChatThreads();
    }

    private void loadChatThreads() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        chatThreads.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Set<String> seenUsers = new HashSet<>();

        db.collection("chats")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("MessagesActivity", "Chats fetched: " + snapshot.size());

                    int totalChats = 0;
                    int[] completedChats = {0};

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String chatId = doc.getId();
                        String[] ids = chatId.split("_");
                        if (ids.length != 2) continue;

                        if (!ids[0].equals(currentUserId) && !ids[1].equals(currentUserId)) continue;

                        String otherUserId = ids[0].equals(currentUserId) ? ids[1] : ids[0];
                        if (seenUsers.contains(otherUserId)) continue;

                        seenUsers.add(otherUserId);
                        totalChats++;

                        int finalTotalChats = totalChats;

                        db.collection("users").document(otherUserId)
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    String username = userSnap.getString("username");

                                    db.collection("chats").document(chatId)
                                            .collection("messages")
                                            .orderBy("timestamp", Query.Direction.DESCENDING)
                                            .limit(1)
                                            .get()
                                            .addOnSuccessListener(messagesSnap -> {
                                                if (!messagesSnap.isEmpty()) {
                                                    String lastMsg = messagesSnap.getDocuments().get(0).getString("message");
                                                    chatThreads.add(new ChatThread(otherUserId, username, lastMsg));
                                                }
                                                completedChats[0]++;
                                                if (completedChats[0] == finalTotalChats) {
                                                    refreshUI();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("MessagesActivity", "Failed messages query", e);
                                                completedChats[0]++;
                                                if (completedChats[0] == finalTotalChats) {
                                                    refreshUI();
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MessagesActivity", "Failed user query", e);
                                    completedChats[0]++;
                                    if (completedChats[0] == finalTotalChats) {
                                        refreshUI();
                                    }
                                });
                    }

                    if (totalChats == 0) {
                        // No chats for this user
                        refreshUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MessagesActivity", "Failed chats fetch", e);
                    refreshUI();
                });
    }

    private void refreshUI() {
        progressBar.setVisibility(View.GONE);
        if (chatThreads.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}
