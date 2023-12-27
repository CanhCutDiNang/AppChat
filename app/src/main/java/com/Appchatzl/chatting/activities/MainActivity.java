package com.Appchatzl.chatting.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.adapter.RecentConversationsAdapter;
import com.Appchatzl.chatting.databinding.ActivityMainBinding;
import com.Appchatzl.chatting.listeners.ConversionListener;
import com.Appchatzl.chatting.models.ChatMessage;
import com.Appchatzl.chatting.models.User;
import com.Appchatzl.chatting.utilities.Constants;
import com.Appchatzl.chatting.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private ImageView imageProfile;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        viewBinding();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();

    }

    private void listenConversations(){
//        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
//                .addSnapshotListener(eventListener);
//        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
//                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .addSnapshotListener(eventListener);
    }
///doan chat gui tin nhan gui hinh
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    String receiverIdGR = documentChange.getDocument().getString(Constants.KEY_RECEIVER_GROUP_ID);
                    boolean isChatGroup = documentChange.getDocument().getBoolean(Constants.KEY_IS_GROUP);
                    if (receiverIdGR != null && !receiverIdGR.contains(preferenceManager.getString(Constants.KEY_USER_ID))) {
                        continue;
                    }else
                    if (!receiverId.equals(preferenceManager.getString(Constants.KEY_USER_ID))
                            && !senderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))
                            && receiverIdGR == null) {
                        continue;
                    }
//                    String arr[] = receiverIdGR.split(",");

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverIdGR;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        if (isChatGroup) {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME_GR);
                        }else {
                            //chat cá nhân
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        }
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        if (isChatGroup) {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME_GR);
                        }else {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        }
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.conversionId = documentChange.getDocument().getId();
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.isGroup = isChatGroup;

                    if (conversations != null && conversations.size() == 0) {
                        conversations.add(chatMessage);
                    }else if (conversations != null && conversations.size() > 0) {
                        boolean isDiff = true;
                        for (ChatMessage item: conversations) {
                            if (item.conversionId.equals(chatMessage.conversionId)) {
                                isDiff = false;
                                break;
                            }
                        }
                        if (isDiff) {
                            conversations.add(chatMessage);
                        }
                    }
                }
                else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i<conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void viewBinding() {
        imageProfile = findViewById(R.id.imageProfile);
    }
//tao group
    private void setListeners() {
        binding.fabNewChat.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.fabNewChatGroup.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            intent.putExtra("group", true);
            startActivity(intent);
        });
        binding.imageProfile.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), PersonalActivity.class)));
    }
//list ra toan bo user
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
//        imageProfile.setImageBitmap(bitmap);
//        Toast.makeText(getApplicationContext(), "Bitmap: " + bitmap, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
//lay token moi 1 tk se co 1 token rieng
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
            db.collection(Constants.KEY_COLLECTION_USERS).document(
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        documentReference.update(Constants.KEY_FCM_TOKEN, token);
//            .addOnFailureListener(e -> showToast("Unable to update token!"));
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
//
    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        //lấy id để chat ca nhan
        intent.putExtra(Constants.KEY_CONVERSION_ID, user.id);
        startActivity(intent);
    }

    @Override
    public void onConversionDelete(ChatMessage chatMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhóm")
                .setMessage("Bạn có chắc chắn muốn xóa?")

                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(
                                    chatMessage.conversionId).delete() .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    conversations.remove(chatMessage);
                                    conversationsAdapter.notifyDataSetChanged();
                                    Toast.makeText(MainActivity.this, "xoá thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                })

        //null cho phép nút loại bỏ hộp thoại và không thực hiện thêm hành động nào.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onBackPressed() {

    }
}