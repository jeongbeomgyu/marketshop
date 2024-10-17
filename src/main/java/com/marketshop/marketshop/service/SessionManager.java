package com.marketshop.marketshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Slf4j
@Service
public class SessionManager {

    // 방별로 여러 사용자 세션을 관리하는 구조 (여러 세션을 List로 관리)
    private final Map<Long, Map<Long, List<WebSocketSession>>> roomList = new HashMap<>();

    public void addSession(Long roomId, Long userNumber, WebSocketSession session) {
        roomList.computeIfAbsent(roomId, k -> new HashMap<>());

        // 특정 사용자에 대한 세션 목록을 가져오거나, 없으면 새로 생성
        Map<Long, List<WebSocketSession>> userSessions = roomList.get(roomId);
        userSessions.computeIfAbsent(userNumber, k -> new ArrayList<>()).add(session);

        log.info("Room ID {}에 사용자 {}의 세션이 추가되었습니다. 현재 세션 목록: {}", roomId, userNumber, roomList.get(roomId));
    }

    // 특정 유저의 첫 번째 세션을 가져옴 (여러 세션 중 첫 번째만 사용)
    public WebSocketSession getSession(Long roomId, Long userNumber) {
        Map<Long, List<WebSocketSession>> chatRoom = roomList.get(roomId);
        if (chatRoom == null) {
            return null;
        }
        List<WebSocketSession> userSessions = chatRoom.get(userNumber);
        return (userSessions == null || userSessions.isEmpty()) ? null : userSessions.get(0);  // 첫 번째 세션 반환
    }

    // 현재 사용자를 제외한 상대방의 ID를 반환하는 메서드
    public Long getOtherUserIdInRoom(Long roomId, Long currentUserId) {
        Map<Long, List<WebSocketSession>> chatRoomSessions = roomList.get(roomId);
        if (chatRoomSessions != null) {
            for (Long userId : chatRoomSessions.keySet()) {
                if (!userId.equals(currentUserId)) {
                    return userId; // 현재 사용자가 아닌 상대방 ID 반환
                }
            }
        }
        return null; // 상대방이 없을 경우 null 반환
    }

    // 세션 제거 메서드
    public void removeSession(Long roomId, Long userNumber, WebSocketSession session) {
        Map<Long, List<WebSocketSession>> chatRoomSessions = roomList.get(roomId);
        if (chatRoomSessions == null) {
            log.warn("roomId {}에 대한 세션 정보가 없습니다.", roomId);
            return;
        }

        List<WebSocketSession> userSessions = chatRoomSessions.get(userNumber);
        if (userSessions != null) {
            userSessions.remove(session);  // 특정 세션만 제거
            if (userSessions.isEmpty()) {
                chatRoomSessions.remove(userNumber);  // 모든 세션이 제거되면 해당 사용자 삭제
            }
        }

        if (chatRoomSessions.isEmpty()) {
            roomList.remove(roomId);
            log.info("roomId {}가 모두 제거되었습니다.", roomId);
        }
    }

    // 채팅방의 모든 세션 반환 (필요한 경우에만 사용)
    public List<WebSocketSession> getAllSessionsInRoom(Long roomId) {
        Map<Long, List<WebSocketSession>> sessions = roomList.get(roomId);
        if (sessions == null) {
            return Collections.emptyList();
        }
        List<WebSocketSession> allSessions = new ArrayList<>();
        for (List<WebSocketSession> userSessions : sessions.values()) {
            allSessions.addAll(userSessions);  // 각 사용자의 모든 세션을 합침
        }
        return allSessions;
    }



//    public void removeSession(Long roomId, Long userNumber){
//
//        roomList.get(roomId).remove(userNumber);
//
//
//    }

}
