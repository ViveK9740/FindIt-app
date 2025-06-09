package com.findit.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.FeedAdapter;
import com.findit.app.FeedItem;
import com.findit.app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewFeed;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedItemList;
    private FirebaseFirestore db;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewFeed = view.findViewById(R.id.recyclerViewFeed);
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(getContext()));

        feedItemList = new ArrayList<>();
        feedAdapter = new FeedAdapter(getContext(), feedItemList);
        recyclerViewFeed.setAdapter(feedAdapter);

        db = FirebaseFirestore.getInstance();
        fetchFeedItemsFromFirestore();

        return view;
    }

    private void fetchFeedItemsFromFirestore() {
        db.collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    feedItemList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getString("userId");
                        String username = doc.getString("userName");
                        String title = doc.getString("title");

                        Timestamp timestamp = doc.getTimestamp("time");
                        long time = timestamp != null ? ((Timestamp) timestamp).toDate().getTime() : 0;

                        String imageUrl = doc.getString("imageUrl");

                        String description = doc.getString("description");

                        Log.d("FirestoreDebug", "Fetched item: " + title);

                        FeedItem item = new FeedItem(userId, username, title, time, description, imageUrl);
                        feedItemList.add(item);
                    }

                    feedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("FirestoreDebug", "Error loading data: " + e.getMessage())
                );
    }
}
