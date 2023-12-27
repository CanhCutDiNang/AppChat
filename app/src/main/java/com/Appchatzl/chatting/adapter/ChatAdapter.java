package com.Appchatzl.chatting.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Appchatzl.chatting.databinding.ItemContainerReceivedMessageBinding;
import com.Appchatzl.chatting.databinding.ItemContainerSentMessageBinding;
import com.Appchatzl.chatting.models.ChatMessage;
import com.Appchatzl.chatting.models.User;
import com.Appchatzl.chatting.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private static List<User> userList = new ArrayList<>();
    private final String senderId;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(Bitmap receiverProfileImage, List<ChatMessage> chatMessages, String senderId) {
        this.receiverProfileImage = receiverProfileImage;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
            );
        }else {
            return  new ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if (TextUtils.isEmpty(chatMessage.messageImage)) {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textDateTime.setVisibility(View.VISIBLE);
                binding.imvShow.setVisibility(View.GONE);
            }else {
                binding.textMessage.setVisibility(View.GONE);
                binding.textDateTime.setVisibility(View.GONE);
                binding.imvShow.setVisibility(View.VISIBLE);
                binding.imvShow.setImageBitmap(getBitmapFromEncodedString(chatMessage.messageImage));
            }
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if (userList != null && userList.size() > 0) {
                for (User user: userList) {
                    if (user.id.equals(chatMessage.senderId)) {
                        binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(user.image));
                        break;
                    }
                }
            }else {
                if (receiverProfileImage != null){
                    binding.imageProfile.setImageBitmap(receiverProfileImage);
                }
            }
            if (TextUtils.isEmpty(chatMessage.messageImage)) {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textDateTime.setVisibility(View.VISIBLE);
                binding.imvShow.setVisibility(View.GONE);
            }else {
                binding.textMessage.setVisibility(View.GONE);
                binding.textDateTime.setVisibility(View.GONE);
                binding.imvShow.setVisibility(View.VISIBLE);
                binding.imvShow.setImageBitmap(getBitmapFromEncodedString(chatMessage.messageImage));
            }
        }
    }

    static Bitmap getBitmapFromEncodedString(String encodedImage){
        if (encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }else {
            return null;
        }
    }
}
