package com.Appchatzl.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.databinding.ActivitySecurityAuthenBinding;
import com.Appchatzl.chatting.databinding.ActivitySignInBinding;
import com.Appchatzl.chatting.databinding.ActivityUsersBinding;

public class SecurityAuthen extends AppCompatActivity {

    private ActivitySecurityAuthenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityAuthenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCheckOTP()){
                    if (!binding.inputOTP.getText().toString().equals("123456")){
                        showToast("Mã OTP không chính xác!");
                    }else {
                        showToast("Xác thực thành công!");
                        startActivity(new Intent(SecurityAuthen.this, ResetActivity.class));
                    }
                }else {

                }
            }
        });
        binding.sendOTP.setOnClickListener(view -> {
            if (isCheckMail()){
                Toast.makeText(getApplicationContext(), "Đã gửi mã!", Toast.LENGTH_SHORT).show();
                binding.sendOTP.setClickable(false);
                binding.inputOTP.setEnabled(true);
                CountDownTimer Timer = new CountDownTimer(60000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        binding.countDown.setText(" "+ millisUntilFinished / 1000 + "s");
                    }
                    public void onFinish() {
                        binding.countDown.setText(" 0!");
                        binding.sendOTP.setClickable(true);
                    }
                }.start();
            }else{
                return;
            }
        });
        binding.textGoBack.setOnClickListener(view -> finish());
    }

    public Boolean isCheckOTP(){
        if(binding.inputOTP.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập mã OTP!");
            return false;
        }else{
            return true;
        }
    }

    public Boolean isCheckMail(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Vui lòng nhập địa chỉ email!");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Vui lòng nhập địa chỉ email phù hợp!");
            return false;
        }else{
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}