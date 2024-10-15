package com.marketshop.marketshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        Map<Long, WebSocketSession> chatRoomSessions = roomList.get(roomId);

        if (chatRoomSessions == null) {
            log.warn("roomId {}에 대한 세션 정보가 없습니다.", roomId);
            return;  // 해당 roomId에 대한 세션이 없으면 바로 반환
        }

        chatRoomSessions.remove(userNumber);

        // 세션이 모두 제거된 경우 해당 roomId 자체를 roomList에서 제거할 수 있음
        if (chatRoomSessions.isEmpty()) {
            roomList.remove(roomId);
            log.info("roomId {}가 모두 제거되었습니다.", roomId);
        }
    }

//    public void removeSession(Long roomId, Long userNumber){
//
//        roomList.get(roomId).remove(userNumber);
//
//
//    }

}
