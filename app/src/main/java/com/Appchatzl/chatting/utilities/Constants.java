package com.Appchatzl.chatting.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "users";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_CONVERSION_ID = "conversionId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_USER_LST = "userList";
    public static final String KEY_LIST_USER_PR = "KEY_LIST_USER_PR";
    public static final String KEY_LIST_USER = "KEY_LIST_USER";
    public static final String KEY_USER_GROUP = "KEY_USER_GROUP";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_RECEIVER_GROUP_ID = "receiverGroupId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_MESSAGE_IMAGE = "messageImage";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_IS_GROUP = "isGroup";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_RECEIVER_NAME_GR = "receiverNameGroup";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "Authorization";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String , String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if (remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAAZlAL0I:APA91bHyer0aLKbX9_rBnUdLYbeC3H8taGFsBwtpuv2A0UQpXIpq4Q1BOe00C5EzpuEVU0cJ__sU9_uMZe3Ge6dZltB2SBHKsAUixuzArVe0nAzYHoGpLOzvApWYPaF9h-ICKRMfQrbw"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
