package com.Appchatzl.chatting.models;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, dateTime, messageImage, receiverGroupId;
    public Date dateObject;
    public String conversionId, conversionName, conversionImage;
    public boolean isGroup;
    public ArrayList<User> listUser;

    public ArrayList<String> getListIdGr() {
        ArrayList<String> list = new ArrayList<>();
        if (TextUtils.isEmpty(receiverGroupId))
            return null;
        String[] arr = receiverGroupId.split(",");
        list.addAll(Arrays.asList(arr));
        return list;
    }

    public boolean isChatGroup() {
        if (TextUtils.isEmpty(receiverGroupId))
            return false;
        String[] arr = receiverGroupId.split(",");
        return arr.length != 2;
    }
}
