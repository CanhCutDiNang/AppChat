package com.Appchatzl.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.databinding.ActivityDetailsChatBinding;
import com.Appchatzl.chatting.databinding.ActivityMainBinding;
import com.Appchatzl.chatting.models.User;
import com.Appchatzl.chatting.utilities.Constants;

public class DetailsChatActivity extends AppCompatActivity {

    private User receiverUser;
    private ActivityDetailsChatBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.nameProfile.setText(receiverUser.name);
    }
}