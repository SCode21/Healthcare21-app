package com.example.signuploginfirebasee;

public class ChatUtils {
    public static String getChatRoomId(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }
}