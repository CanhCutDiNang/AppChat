package com.Appchatzl.chatting.activities;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.adapter.UsersAdapter;
import com.Appchatzl.chatting.databinding.ActivityUsersBinding;
import com.Appchatzl.chatting.listeners.UserListener;
import com.Appchatzl.chatting.models.User;
import com.Appchatzl.chatting.utilities.Constants;
import com.Appchatzl.chatting.utilities.PreferenceManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    RecyclerView recyclerView;
    private boolean isGroup = false;
    UsersAdapter usersAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_users);
        isGroup = getIntent().getBooleanExtra("group", false);
        preferenceManager = new PreferenceManager(getApplicationContext());
        recyclerView = findViewById(R.id.usersRecyclerView);
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        findViewById(R.id.imageBack).setOnClickListener(view -> onBackPressed());
        if (isGroup) {
            findViewById(R.id.tvAdd).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.tvAdd).setVisibility(View.GONE);
        }
        findViewById(R.id.tvAdd).setOnClickListener(view -> {
            if (usersAdapter != null && usersAdapter.getUserSelected().size() > 0){
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                usersAdapter.getUserSelected().get(0).idGroup = usersAdapter.getUserIDSelected()+","+preferenceManager.getString(Constants.KEY_USER_ID);
                if (usersAdapter.getUserSelected().size() == 1) {
                    intent.putExtra(Constants.KEY_USER, usersAdapter.getUserSelected().get(0));
                }else {
                    usersAdapter.getUserSelected().get(0).nameGroup = usersAdapter.getGrNameSelected();
                    usersAdapter.getUserSelected().get(0).listUser = usersAdapter.getUserSelected();
                    intent.putExtra(Constants.KEY_USER, usersAdapter.getUserSelected().get(0));
                    intent.putExtra(Constants.KEY_USER_GROUP, usersAdapter.getUserIDSelected()+","+"");
                }
                startActivity(intent);
                finish();
            }else {
                Toast.makeText(this, "Vui lòng chọn người vào nhóm!!", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
        .get()
        .addOnCompleteListener(task -> {
            loading(false);
            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (task.isSuccessful() && task.getResult() != null){
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    if (currentUserId.equals(queryDocumentSnapshot.getId())){
                        continue;
                    }
                    User user = new User();
                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                    user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    user.id = queryDocumentSnapshot.getId();
                    users.add(user);
                }
                if (users.size()>0){
                    preferenceManager.putString(Constants.KEY_LIST_USER_PR, new Gson().toJson(users));
                    usersAdapter = new UsersAdapter(users, this);
                    usersAdapter.setGroup(isGroup);
                    binding.usersRecyclerView.setAdapter(usersAdapter);
                    binding.usersRecyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(usersAdapter);
                    recyclerView.setVisibility(View.VISIBLE);
                }else{
                    showErrorMessage();
                }
            }else {
                showErrorMessage();
            }
        });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available!"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}