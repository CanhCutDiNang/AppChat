package com.Appchatzl.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.databinding.ActivityMainBinding;
import com.Appchatzl.chatting.databinding.ActivityPersonalBinding;
import com.Appchatzl.chatting.utilities.Constants;
import com.Appchatzl.chatting.utilities.PreferenceManager;

import java.util.HashMap;

public class PersonalActivity extends AppCompatActivity {

    private ActivityPersonalBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        setListeners();
    }

    private void setListeners() {
        binding.toolbarProfile.setOnClickListener(view -> onBackPressed());
        binding.layoutLogout.setOnClickListener(v -> signOut());
    }

    private void signOut(){
//        showToast("Đăng xuất...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                });
//            .addOnFailureListener(e -> showToast("Unable to sign out!"));
    }

    private void loadUserDetails(){
        binding.nameProfile.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.emailProfile.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
}