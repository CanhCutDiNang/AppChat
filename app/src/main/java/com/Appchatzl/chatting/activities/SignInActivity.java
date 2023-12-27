package com.Appchatzl.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.databinding.ActivitySignInBinding;
import com.Appchatzl.chatting.utilities.Constants;
import com.Appchatzl.chatting.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private static final String FILE_NAME = "myFile";
    private CheckBox remember_me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonSigIn.setOnClickListener(view -> {
            if (isValidSignInDetails()){
                sigIn();
            }
        });
        binding.textResetAccount.setOnClickListener(view -> startActivity(new Intent(SignInActivity.this, SecurityAuthen.class)));
        SharedPreferences sharedPreferences = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String edittext_phone = sharedPreferences.getString("edittext_phone", "");
        String edittext_password = sharedPreferences.getString("edittext_password", "");
        binding.inputEmail.setText(edittext_phone);
        binding.inputPassword.setText(edittext_password);
    }

    private void Remember(String edittext_phone, String edittext_password) {
        if (binding.rememberMe.isChecked()){
            SharedPreferences.Editor editor = getSharedPreferences(FILE_NAME, MODE_PRIVATE).edit();
            editor.putString("edittext_phone",edittext_phone);
            editor.putString("edittext_password",edittext_password);
            editor.apply();
        }else {
            SharedPreferences.Editor editor = getSharedPreferences(FILE_NAME, MODE_PRIVATE).edit();
            editor.putString("edittext_phone","");
            editor.putString("edittext_password","");
            editor.apply();
        }
    }

    private void sigIn(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() >0){
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                    preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                    Remember(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    loading(false);
//                        showToast("Unable to sign in!");
                }
            });
    }

    private void loading(Boolean isloading){
        if (isloading){
            binding.buttonSigIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSigIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Vui lòng nhập địa chỉ email!");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Vui lòng nhập địa chỉ email phù hợp!");
            return false;
        }else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Vui lòng nhập mật khẩu đăng nhập!");
            return false;
        }else{
            return true;
        }
    }
}