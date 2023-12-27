package com.Appchatzl.chatting.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;
import com.Appchatzl.chatting.R;
import com.Appchatzl.chatting.adapter.ChatAdapter;
import com.Appchatzl.chatting.models.ChatMessage;
import com.Appchatzl.chatting.models.User;
import com.Appchatzl.chatting.network.ApiClient;
import com.Appchatzl.chatting.network.ApiService;
import com.Appchatzl.chatting.utilities.Constants;
import com.Appchatzl.chatting.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

        private User receiverUser;
        private TextView textName, textAvailability;
        private RoundedImageView imageProfileChat;
        private AppCompatImageView imageBack, imageInfo, imageVideo, imageCall;
        private RecyclerView chatRecyclerView;
        private EditText inputMessage;
        private ProgressBar progressBar;
        private FrameLayout layoutSend, layoutImage;
        public List<ChatMessage> chatMessages;
        private ChatAdapter chatAdapter;
        private PreferenceManager preferenceManager;
        private FirebaseFirestore database;
        private String conversionId = null;
        private boolean isRegisterMess = false;
    private Boolean isReceiverAvailable = false;
    private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        viewBinding();
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void listenMessages(){
        if (!TextUtils.isEmpty(conversionId)) {
            isRegisterMess = true;
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_CONVERSION_ID, conversionId)
                    .addSnapshotListener(eventListener);
        }else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body()!=null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) result.get(0);
//                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    showToast("Notification successfully");
                }
                else {
//                    showToast("Error: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                showToast("Lỗi:"  + t.getMessage());
            }
        });

    }

    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
           if(error != null){
               return;
           }
           if (value != null){
               if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                   int availability = Objects.requireNonNull(
                           value.getLong(Constants.KEY_AVAILABILITY)
                   ).intValue();
                   isReceiverAvailable = availability == 1;
               }
               receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
               if (receiverUser.image == null){
                   receiverUser.image = value.getString(Constants.KEY_IMAGE);
                   chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                   chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
               }
           }
           if (isReceiverAvailable){
               textAvailability.setVisibility(View.VISIBLE);
           }else {
               textAvailability.setText("Không trực tuyến");
           }
        });
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.messageImage = documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE);
                    chatMessage.receiverGroupId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_GROUP_ID);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, Comparator.comparing(obj -> obj.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            chatRecyclerView.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    };

    private String endcodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
                    , result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            if (result.getData() != null) {
                                Uri imageUri = result.getData().getData();
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    encodedImage = endcodeImage(bitmap);

                                    if (conversionId != null) {
                                        updateConversion(inputMessage.getText().toString());
                                        sendText("");
                                    } else {
                                        HashMap<String, Object> conversion = new HashMap<>();
                                        conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                                        conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                                        conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                                        if (!TextUtils.isEmpty(receiverUser.idGroup)) {
                                            conversion.put(Constants.KEY_RECEIVER_GROUP_ID, receiverUser.idGroup);
//                                            conversion.put(Constants.KEY_LIST_USER, new Gson().toJson(receiverUser.listUser));
                                        }
                                        conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                                        conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                                        if (!TextUtils.isEmpty(receiverUser.nameGroup)) {
                                            conversion.put(Constants.KEY_RECEIVER_NAME_GR, receiverUser.nameGroup);
                                        }
                                        conversion.put(Constants.KEY_MESSAGE_IMAGE, encodedImage);
                                        conversion.put(Constants.KEY_LAST_MESSAGE, inputMessage.getText().toString());
                                        conversion.put(Constants.KEY_TIMESTAMP, new Date());
                                        conversion.put(Constants.KEY_IS_GROUP, receiverUser.isChatGroup());
                                        addConversion(conversion,"");
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

    private void addConversion(HashMap<String, Object> conversion, String mess){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> {
                    conversionId = documentReference.getId();
                    if (!isRegisterMess) {
                        listenMessages();
                    }
                    sendText(mess);
                });
    }

    private void sendText(String mess) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        if (!TextUtils.isEmpty(receiverUser.idGroup)) {
            message.put(Constants.KEY_RECEIVER_GROUP_ID, receiverUser.idGroup);
//            message.put(Constants.KEY_LIST_USER, new Gson().toJson(receiverUser.listUser));

        }
        if (!TextUtils.isEmpty(encodedImage)) {
            message.put(Constants.KEY_MESSAGE_IMAGE, encodedImage);
            encodedImage = "";
        }
        message.put(Constants.KEY_MESSAGE, mess);
        if (!TextUtils.isEmpty(conversionId)) {
            message.put(Constants.KEY_CONVERSION_ID, conversionId);
        }
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
    }

    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );

    }

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if (encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }else {
            return null;
        }
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                getBitmapFromEncodedString(receiverUser.image),
                chatMessages,
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        chatAdapter.setUserList(new Gson().fromJson(preferenceManager.getString(Constants.KEY_LIST_USER_PR), new TypeToken<List<User>>(){}.getType()));
        chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){

        if (conversionId != null){
            updateConversion(inputMessage.getText().toString());
            sendText(inputMessage.getText().toString());
        }else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            if (!TextUtils.isEmpty(receiverUser.idGroup)) {
                conversion.put(Constants.KEY_RECEIVER_GROUP_ID, receiverUser.idGroup);
//                conversion.put(Constants.KEY_LIST_USER, new Gson().toJson(receiverUser.listUser));
            }
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            if (!TextUtils.isEmpty(receiverUser.nameGroup)) {
                conversion.put(Constants.KEY_RECEIVER_NAME_GR, receiverUser.nameGroup);
            }
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            conversion.put(Constants.KEY_IS_GROUP, receiverUser.isChatGroup());
            addConversion(conversion, inputMessage.getText().toString());
        }

        if (!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (JSONException e) {
//                showToast(e.getMessage());
            }

        }
        inputMessage.setText(null);
    }

    private void viewBinding() {
        imageBack = findViewById(R.id.imageBack);
        textName = findViewById(R.id.textName);
        chatRecyclerView = findViewById(R.id.chatRecyclerViewChat);
        inputMessage = findViewById(R.id.inputMessage);
        layoutSend = findViewById(R.id.layoutSend);
        progressBar = findViewById(R.id.progressBar);
        textAvailability = findViewById(R.id.textAvailability);
        imageProfileChat = findViewById(R.id.imageProfileChat);
        imageInfo = findViewById(R.id.imageInfo);
        imageVideo = findViewById(R.id.imageVideo);
        imageCall = findViewById(R.id.imageCall);
        layoutImage = findViewById(R.id.layoutImage);
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        if (getIntent().hasExtra(Constants.KEY_CONVERSION_ID)) {
            conversionId = getIntent().getStringExtra(Constants.KEY_CONVERSION_ID);
        }
        byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageProfileChat.setImageBitmap(bitmap);
        if (!TextUtils.isEmpty(receiverUser.nameGroup)) {
            textName.setText(receiverUser.nameGroup);
        }else {
            textName.setText(receiverUser.name);
        }
    }
//bo trong tin nhan
    private void setListeners() {
        imageBack.setOnClickListener(v -> onBackPressed());
        imageBack.setOnClickListener(v -> onBackPressed());
        findViewById(R.id.layoutSend).setOnClickListener(view -> {
            if (!inputMessage.getText().toString().trim().equals("")){
                sendMessage();
            }else{
                showToast("Vui lòng nhập nội dung tin nhắn!");
                return;
            }
        });
        imageInfo.setOnClickListener(view -> {
            User user = new User();
            if (!TextUtils.isEmpty(receiverUser.nameGroup)) {
                user.name = receiverUser.nameGroup;
            }else {
                user.name = receiverUser.name;
            }
            user.image = receiverUser.image;
            user.id = receiverUser.id;
            user.chat = String.valueOf(chatMessages.size());
            Intent intent = new Intent(getApplicationContext(), DetailsChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        });
        imageCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();
                if (!TextUtils.isEmpty(receiverUser.nameGroup)) {
                    user.name = receiverUser.nameGroup;
                }else {
                    user.name = receiverUser.name;
                }
                user.image = receiverUser.image;
                user.id = receiverUser.id;
                Intent intent = new Intent(getApplicationContext(), CallInActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
        });
        imageVideo.setOnClickListener(view -> Toast.makeText(getApplicationContext(), "Chức năng gọi video", Toast.LENGTH_SHORT).show());
        layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    public String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void checkForConversion(){
        if (chatMessages.size()!= 0){
            if (getIntent().hasExtra(Constants.KEY_USER_GROUP)) {
                checkForConversionRemotelyGroup(
                        preferenceManager.getString(Constants.KEY_USER_ID),
                        receiverUser.idGroup
                );
                checkForConversionRemotelyGroup(
                        receiverUser.idGroup,
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
            }else {
                checkForConversionRemotely(
                        preferenceManager.getString(Constants.KEY_USER_ID),
                        receiverUser.id
                );
                checkForConversionRemotely(
                        receiverUser.id,
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
            }

        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private void checkForConversionRemotelyGroup(String senderId, String receiverGrId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_GROUP_ID, receiverGrId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}