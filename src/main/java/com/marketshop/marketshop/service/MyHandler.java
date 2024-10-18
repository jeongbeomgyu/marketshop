package com.marketshop.marketshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marketshop.marketshop.config.JwtTokenProvider;
import com.marketshop.marketshop.controller.WebChatController;
import com.marketshop.marketshop.entity.ChatMessage;
import com.marketshop.marketshop.entity.ChatRoom;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.repository.ChatRoomRepository;
import com.marketshop.marketshop.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SessionAttributes("member")
@Slf4j
public class MyHandler extends TextWebSocketHandler {

    Map<String, WebSocketSession> sessionMap = new HashMap<>(); //웹소켓 세션을 담아둘 맵

    @Autowired
    ChatRoomService chatRoomService;

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    WebChatController webChatController;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MemberRepository userService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;  // ChatRoomRepository 의존성 주입 추가



    // 소켓 연결이 완료됐을 때 실행되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("연결 완료");

        // URI에서 쿼리스트링 추출
        String query = session.getUri().getQuery();
        if (query == null || !query.contains("token=")) {
            log.error("JWT 토큰이 존재하지 않음");
            session.close(CloseStatus.BAD_DATA); // 잘못된 요청으로 소켓을 닫음
            return;
        }

        // 토큰 추출
        String token = query.split("token=")[1];
        if (token == null || token.isEmpty()) {
            log.error("JWT 토큰이 존재하지 않음");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            log.error("JWT 토큰이 유효하지 않음");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 토큰에서 사용자 정보 추출
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String userEmail = authentication.getName();
        Member sender = userService.findByEmail(userEmail);  // 사용자 정보 가져오기
        Long senderId = sender.getId();
        log.info("WebSocket 연결된 사용자: {}", userEmail);

        // URI에서 roomId와 userNumber 추출
        String path = session.getUri().getPath();
        String[] segments = path.split("/");
        if (segments.length < 4) {
            log.error("잘못된 WebSocket URI: {}", path);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Long roomId = Long.parseLong(segments[2]);
        Long userNumber = Long.parseLong(segments[3]);

        log.info("연결된 소켓의 RoomId는 {}", roomId);

        // 채팅방 권한 확인 (채팅방에 대한 권한이 있는지 확인)
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        // 사용자 권한 확인 (판매자나 구매자인지 확인)
        if (!chatRoom.getUser1().getId().equals(senderId) && !chatRoom.getUser2().getId().equals(senderId)) {
            log.error("사용자 {}는 채팅방 {}에 접근 권한이 없습니다.", senderId, roomId);
            session.close(CloseStatus.NOT_ACCEPTABLE);  // 접근 권한이 없으면 연결 차단
            return;
        }

        // 세션 추가
        sessionManager.addSession(roomId, userNumber, session);
        log.info("사용자 {}가 채팅방 {}에 접속했습니다.", userNumber, roomId);
    }

    // 소켓 연결이 완료됐을 때 실행되는 메서드
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//
//
//        log.info("연결완료");
//
//        String str = session.getUri().getPath().substring((session.getId().lastIndexOf("/")) + 5);
//        Long roomId = Long.parseLong(str.split("/")[0]);
//        Long userNumber = Long.parseLong(str.split("/")[1]);
//
//        log.info("연결된 소켓의 RoomId는 {}",roomId);
//
//        sessionManager.addSession(roomId, userNumber, session);
//
//    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // URI에서 roomId와 userNumber 추출
        String path = session.getUri().getPath();
        String[] segments = path.split("/");

        if (segments.length < 3) {
            log.error("URI 형식이 잘못되었습니다: {}", path);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Long roomId = Long.parseLong(segments[2]);

        try {
            // JWT 토큰에서 사용자 정보 추출
            String query = session.getUri().getQuery();
            String token = query != null && query.contains("token=") ? query.split("token=")[1] : null;

            if (token == null || token.isEmpty()) {
                log.error("JWT 토큰이 존재하지 않음");
                session.close(CloseStatus.BAD_DATA);
                return;
            }

            // JWT 토큰을 검증하고 사용자 정보 추출
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String userEmail = authentication.getName();
            Member sender = userService.findByEmail(userEmail);
            Long senderId = sender.getId();

            // 채팅방 존재 여부 확인
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

            // 사용자 권한 확인 (판매자나 구매자인지 확인)
            if (!chatRoom.getUser1().getId().equals(senderId) && !chatRoom.getUser2().getId().equals(senderId)) {
                log.error("사용자 {}는 채팅방 {}에 접근 권한이 없습니다.", senderId, roomId);
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 메시지 파싱
            log.info("수신된 메시지 payload: {}", message.getPayload());
            Map<String, Object> jsonMessage = objectMapper.readValue(message.getPayload(), Map.class);

            // 메시지 타입 확인
            String type = (String) jsonMessage.get("type");
            if ("close".equals(type)) {
                sessionManager.removeSession(roomId, senderId, session); // 세션 제거
                return;
            }

            // 메시지 내용 처리
            String content = (String) jsonMessage.get("content");
            if (content == null || content.isEmpty()) {
                log.error("메시지 내용이 비어있습니다.");
                session.close(CloseStatus.BAD_DATA);
                return;
            }

            // 메시지 저장
            ChatMessage chatMessage = chatMessageService.saveMessage(roomId, senderId, content);
            String parseSendMessage = objectMapper.writeValueAsString(chatMessage);

            // 응답 메시지 구성
            Map<String, Object> responseMessage = new HashMap<>();
            responseMessage.put("sender", sender.getName());
            responseMessage.put("messageId", chatMessage.getMessageId());
            responseMessage.put("content", chatMessage.getContent());
            responseMessage.put("roomId", roomId);
            responseMessage.put("sendTime", chatMessage.getSendTime());

            // 현재 사용자를 제외한 상대방 ID 가져오기
            Long receiverId = sessionManager.getOtherUserIdInRoom(roomId, senderId);
            if (receiverId != null) {
                WebSocketSession receiverSession = sessionManager.getSession(roomId, receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    // 상대방에게 메시지 전송
                    log.info("세션 {}에 메시지 전송", receiverSession.getId());
                    receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMessage)));
                } else {
                    log.warn("상대방의 세션이 닫혀있거나 존재하지 않습니다.");
                }
            } else {
                log.warn("상대방이 채팅방에 존재하지 않습니다.");
            }

        } catch (IOException e) {
            log.error("메시지 파싱 오류", e);
            session.close(CloseStatus.BAD_DATA);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        System.out.println("error");
        ;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

        log.info("핸들러에 의해 remove 메서드 실행(비정상적인 경로로 종료 / reload, 페이지 종료 등)");
        String str = session.getUri().getPath().substring((session.getId().lastIndexOf("/")) + 5);

        // URI에서 roomId와 userNumber를 추출
        Long roomId = Long.parseLong(str.split("/")[0]);
        Long userNumber = Long.parseLong(str.split("/")[1]);

        // 세션 제거를 위해 WebSocketSession 객체를 넘김
        sessionManager.removeSession(roomId, userNumber, session);  // WebSocketSession 객체 추가
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
