package com.Appchatzl.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.databinding.ActivityResetBinding;
import com.Appchatzl.chatting.databinding.ActivitySignInBinding;
import com.Appchatzl.chatting.utilities.PreferenceManager;

public class ResetActivity extends AppCompatActivity {

    private ActivityResetBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.textSignIn.setOnClickListener(view -> startActivity(new Intent(ResetActivity.this, SignInActivity.class)));
        binding.buttonReset.setOnClickListener(view -> {
            if (isValidResetDetails()==true){
                showToast("Thay đổi mật khẩu thành công!");
            }
            else {
                showToast("Vui lòng nhập đầy đủ các thông tin!");
            }
        });
        binding.textGoBack.setOnClickListener(view -> finish());
    }
    private Boolean isValidResetDetails(){
        if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Vui lòng nhập mật khẩu!");
            return false;
        }else  if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Vui lòng xác nhận mật khẩu!");
            return false;
        }
        else  if (!binding.inputConfirmPassword.getText().toString().equals(binding.inputPassword.getText().toString())){
            showToast("Mật khẩu phải trùng nhau!");
            return false;
        }else {
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}