package com.Appchatzl.chatting.models;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    public String name, image, email, token, id, chat, idGroup, nameGroup;
    public boolean isSelected;
    public ArrayList<User> listUser;

    public boolean isChatGroup() {
        if (TextUtils.isEmpty(idGroup))
            return false;
        String[] arr = idGroup.split(",");
        return arr.length != 2;
    }
}
