package com.marketshop.marketshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marketshop.marketshop.config.JwtTokenProvider;
import com.marketshop.marketshop.controller.WebChatController;
import com.marketshop.marketshop.entity.ChatMessage;
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


    // 소켓 연결이 완료됐을 때 실행되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("연결완료");

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

        // 세션 추가
        sessionManager.addSession(roomId, userNumber, session);
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


    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        String type = "";  // 전송된 메시지의 타입.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // URI에서 roomId와 userNumber 추출
        String path = session.getUri().getPath();
        String[] segments = path.split("/");

        if (segments.length < 4) {
            log.error("URI 형식이 잘못되었습니다: {}", path);
            session.close(CloseStatus.BAD_DATA);  // 잘못된 요청이므로 세션 닫기
            return;
        }

        Long roomId = Long.parseLong(segments[2]);
        Long userNumber = Long.parseLong(segments[3]);

        WebSocketSession yourSession = sessionManager.getSession(roomId, userNumber);

        try {
            // 메시지 파싱 (Jackson의 ObjectMapper 사용)
            log.info("수신된 메시지 payload: {}", message.getPayload());
            Map<String, Object> jsonMessage = objectMapper.readValue(message.getPayload(), Map.class);

            // 메시지 타입 확인
            type = (String) jsonMessage.get("type");
            if ("close".equals(type)) { // 소켓 종료 알림 시 세션 제거 후 종료
                sessionManager.removeSession(roomId, userNumber);
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
            ChatMessage chatMessage = chatMessageService.saveMessage(roomId, userNumber, content);
            String parseSendMessage = objectMapper.writeValueAsString(chatMessage);

            // 응답 메시지 구성
            jsonMessage.put("message", parseSendMessage);

            // 상대방에게 메시지 전송
            if (yourSession != null) {
                yourSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(jsonMessage)));
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

        log.info("핸들러에의해 remove 메서드 실행(비정상적인 경로로 종료 / reoload, 페이지 종료 등");
        String str = session.getUri().getPath().substring((session.getId().lastIndexOf("/")) + 5);
        Long roomId = Long.parseLong(str.split("/")[0]);
        Long userNumber = Long.parseLong(str.split("/")[1]);
        sessionManager.removeSession(roomId, userNumber);

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
