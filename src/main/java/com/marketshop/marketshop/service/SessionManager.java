package com.marketshop.marketshop.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Service
public class SessionManager {

    private final Map<Long, Map<Long, WebSocketSession>> roomList = new HashMap<>();

    public void addSession(Long roomId, Long userNumber, WebSocketSession session) {

        Map<Long, WebSocketSession> sessions = new HashMap<>(); // 세션은 그때그떄 다른 걸 넣어줘야한다.

        if (roomList.get(roomId) == null) {
            sessions.put(userNumber, session);
            roomList.put(roomId, sessions);
        } else {
            roomList.get(roomId).put(userNumber, session);
        }

        System.out.println(roomList);

    }

    public WebSocketSession getSession(Long roomId, Long userNumber) {


        Map<Long, WebSocketSession> chatRoom = roomList.get(roomId);
        WebSocketSession session = null;


        for (Long userKey : chatRoom.keySet()) {

            if (!userKey.equals(userNumber)) {
                session = chatRoom.get(userKey);
            }
        }
        return session;
    }

    public void removeSession(Long roomId, Long userNumber){

        roomList.get(roomId).remove(userNumber);


    }

}
