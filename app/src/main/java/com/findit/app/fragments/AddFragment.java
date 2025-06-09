package com.findit.app.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.findit.app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddFragment extends Fragment {

    private EditText titleInput, descriptionInput;
    private Button addButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private Spinner spinnerType;
    private ImageView selectedImageView;
    private Button selectImageButton;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri selectedImageUri = null;
    private String selectedType = "Lost";
    private static final int PICK_IMAGE_REQUEST = 1;

    public AddFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        titleInput = view.findViewById(R.id.editTitle);
        descriptionInput = view.findViewById(R.id.editDescription);
        addButton = view.findViewById(R.id.buttonAdd);
        progressBar = view.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        addButton.setOnClickListener(v -> addPost());

        spinnerType = view.findViewById(R.id.spinnerType);
        selectedImageView = view.findViewById(R.id.selectedImageView);
        selectImageButton = view.findViewById(R.id.buttonSelectImage);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        selectImageButton.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            selectedImageView.setVisibility(View.VISIBLE);
            selectedImageView.setImageURI(selectedImageUri);
        }
    }



    private void addPost() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionInput.setError("Description is required");
            return;
        }

        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        addButton.setEnabled(false);

        String userId = user.getUid();

        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("username");
                    String userEmail = user.getEmail();

                    // Upload image if selected
                    if (selectedImageUri != null) {
                        String imageName = "images/" + UUID.randomUUID().toString();
                        StorageReference imageRef = storageReference.child(imageName);

                        imageRef.putFile(selectedImageUri)
                                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String imageUrl = uri.toString();
                                            uploadPost(title, description, selectedType, userId, userName, userEmail, imageUrl);
                                        }))
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    addButton.setEnabled(true);
                                });
                    } else {
                        uploadPost(title, description, selectedType, userId, userName, userEmail, null);
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    addButton.setEnabled(true);
                });
    }

    private void uploadPost(String title, String description, String type, String userId, String userName, String userEmail, String imageUrl) {
        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("description", description);
        post.put("type", type);
        post.put("time", Timestamp.now());
        post.put("userId", userId);
        post.put("userName", userName);
        post.put("userEmail", userEmail);
        if (imageUrl != null) {
            post.put("imageUrl", imageUrl);
        }

        db.collection("items")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Post added", Toast.LENGTH_SHORT).show();
                    titleInput.setText("");
                    descriptionInput.setText("");
                    selectedImageView.setImageURI(null);
                    selectedImageView.setVisibility(View.GONE);
                    selectedImageUri = null;
                    progressBar.setVisibility(View.GONE);
                    addButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    addButton.setEnabled(true);
                });
    }
}


