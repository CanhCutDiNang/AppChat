package com.Appchatzl.chatting.listeners;

import com.Appchatzl.chatting.models.ChatMessage;
import com.Appchatzl.chatting.models.User;

public interface ConversionListener {
    void onConversionClicked(User user);
    void onConversionDelete(ChatMessage chatMessage);
}
