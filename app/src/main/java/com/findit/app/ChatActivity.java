package com.findit.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.MessageAdapter;
import com.findit.app.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText inputMessage;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private String currentUserId, receiverId, chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MaterialToolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        String chatUserName = getIntent().getStringExtra("chatUserName");
        if (getSupportActionBar() != null && chatUserName != null) {
            getSupportActionBar().setTitle(chatUserName);
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Returns to MainActivity if it's in the back stack
        });


        inputMessage = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerViewMessages);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, FirebaseAuth.getInstance().getCurrentUser().getUid());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");

        chatId = currentUserId.compareTo(receiverId) < 0 ?
                currentUserId + "_" + receiverId : receiverId + "_" + currentUserId;

        loadMessages();

        sendButton.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text);
            }
        });
    }

    private void sendMessage(String text) {
        Message message = new Message(currentUserId, receiverId, text, Timestamp.now());

        Log.d("ChatDebug", "Sending message - Sender: " + currentUserId + ", Receiver: " + receiverId);

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> Log.d("ChatDebug", "Message sent"))
                .addOnFailureListener(e -> Log.e("ChatDebug", "Failed to send message", e));

        inputMessage.setText("");
    }

    private void loadMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }
}
