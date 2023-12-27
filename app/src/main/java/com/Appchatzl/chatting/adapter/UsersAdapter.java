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

import com.Appchatzl.chatting.databinding.ItemContainerUserBinding;
import com.Appchatzl.chatting.listeners.UserListener;
import com.Appchatzl.chatting.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;
    private boolean isGroup = false;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    public ArrayList<User> getUserSelected() {
        ArrayList<User> list = new ArrayList<>();
        for (User item: users) {
            if (item.isSelected) {
                list.add(item);
            }
        }
        return list;
    }
    public String getUserIDSelected() {
        String result = "";
        for (User item: users) {
            if (item.isSelected) {
                if (TextUtils.isEmpty(result)) {
                    result = item.id;
                }else {
                    result = result+ "," +item.id;
                }
            }
        }
        return result;
    }
    public String getGrNameSelected() {
        String result = "";
        for (User item: users) {
            if (item.isSelected) {
                if (TextUtils.isEmpty(result)) {
                    result = item.name;
                }else {
                    result = result+ "," +item.name;
                }
            }
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.name);
//            binding.textEmail.setText(user.email);
            String mail = user.email;
            String[] parts = mail.split("@");
            String part1 = parts[0];
            binding.textEmail.setText("@"+part1);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.imvCheck.setVisibility(user.isSelected ? View.VISIBLE : View.GONE);
            binding.getRoot().setOnClickListener(v -> {
                if (isGroup) {
                    user.isSelected = !user.isSelected;
                    notifyItemChanged(getBindingAdapterPosition());
                }else {
                    userListener.onUserClicked(user);
                }
            });
        }
    }

    private Bitmap getUserImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
