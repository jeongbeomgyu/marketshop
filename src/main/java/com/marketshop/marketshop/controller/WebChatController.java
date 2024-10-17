package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.config.auth.PrincipalDetails;
import com.marketshop.marketshop.dto.ChatMessageDto;
import com.marketshop.marketshop.dto.ChatRoomDto;
import com.marketshop.marketshop.entity.ChatMessage;
import com.marketshop.marketshop.entity.ChatRoom;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.service.ChatMessageService;
import com.marketshop.marketshop.service.ChatRoomService;
import com.marketshop.marketshop.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@SessionAttributes({"user","user1","user2","user3","user4"})
@RequiredArgsConstructor
public class WebChatController {

    @Autowired
    ChatRoomService chatRoomService;

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    MemberService userService;

    @Operation(summary = "채팅 시작", description = "특정 상품과 관련된 채팅방을 생성하거나 기존 채팅방을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 생성 또는 반환 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/item/{itemId}/chat")
    public ResponseEntity<ChatRoomDto> startChat(@PathVariable("itemId") Long itemId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = principal.getName();  // 이메일로 사용자 확인
        Member member = userService.findByEmail(userEmail);

        Long user2Id = member.getId();  // 로그인한 구매자 ID
        ChatRoom chatRoom = chatRoomService.createOrGetChatRoom(itemId, user2Id);

        // ChatRoom 엔티티를 ChatRoomDto로 변환하여 반환
        ChatRoomDto chatRoomDto = ChatRoomDto.fromEntity(chatRoom);
        return ResponseEntity.ok(chatRoomDto);
    }

    // 메시지 전송
    @Operation(summary = "메시지 전송", description = "특정 채팅방에 메시지를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "해당 채팅방에 접근할 수 없음")
    })
    @PostMapping("/chat/{roomId}/message")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable("roomId") Long roomId,
            @RequestBody String content,
            Principal principal) {

        if (principal == null) {
            log.error("Principal is null, user not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = principal.getName();
        log.info("Authenticated user: {}", userEmail); // 사용자 이메일 로그 추가

        Member member = userService.findByEmail(userEmail);
        Long senderId = member.getId();

        // 채팅방 가져오기
        ChatRoom chatRoom = chatRoomService.getChatRoomByRoomId(roomId);
        if (chatRoom == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 채팅방을 찾을 수 없음
        }

        // 채팅방 접근 권한 확인 (user1 또는 user2가 아니라면 403 Forbidden 응답)
        if (!chatRoom.getUser1().getId().equals(senderId) && !chatRoom.getUser2().getId().equals(senderId)) {
            log.error("User {} is not allowed to send messages to room {}", userEmail, roomId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // 접근 권한이 없음
        }

        // 메시지 저장
        ChatMessage message = chatMessageService.saveMessage(roomId, senderId, content);

        // 저장된 메시지를 DTO로 변환하여 반환
        ChatMessageDto messageDto = new ChatMessageDto(message);
        return ResponseEntity.ok(messageDto);
    }

    // 특정 채팅방의 모든 메시지 조회
    @Operation(summary = "채팅방의 모든 메시지 조회", description = "특정 채팅방에 있는 모든 메시지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
            @ApiResponse(responseCode = "403", description = "해당 채팅방에 접근할 수 없음")
    })
    @GetMapping("/chat/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getAllMessagesInRoom(@PathVariable("roomId") Long roomId, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = principal.getName();
        log.info("Authenticated user: {}", userEmail); // 사용자 이메일 로그 추가

        Member member = userService.findByEmail(userEmail);
        Long userId = member.getId();

        // 채팅방 가져오기
        ChatRoom chatRoom = chatRoomService.getChatRoomByRoomId(roomId);
        if (chatRoom == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 채팅방을 찾을 수 없음
        }

        // 채팅방 접근 권한 확인 (user1 또는 user2가 아니라면 403 Forbidden 응답)
        if (!chatRoom.getUser1().getId().equals(userId) && !chatRoom.getUser2().getId().equals(userId)) {
            log.error("User {} is not allowed to view messages in room {}", userEmail, roomId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // 접근 권한이 없음
        }

        // 메시지 조회
        List<ChatMessage> messages = chatMessageService.getAllMessagesInRoom(roomId);
        List<ChatMessageDto> messageDtos = messages.stream()
                .map(ChatMessageDto::new)  // 메시지를 DTO로 변환
                .collect(Collectors.toList());
        return ResponseEntity.ok(messageDtos);
    }

    // roomId로 채팅방 검색 및 입장
    @PostMapping("/room/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoomById(@PathVariable("roomId") Long roomId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = principal.getName();  // 로그인된 사용자의 이메일 확인
        Member member = userService.findByEmail(userEmail);

        // 채팅방 검색
        ChatRoom chatRoom = chatRoomService.getChatRoomByRoomId(roomId);

        // 채팅방이 존재하지 않거나 사용자가 채팅방에 속하지 않은 경우
        if (chatRoom == null || !chatRoom.isParticipant(member)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // ChatRoom 엔티티를 ChatRoomDto로 변환하여 반환
        ChatRoomDto chatRoomDto = ChatRoomDto.fromEntity(chatRoom);
        return ResponseEntity.ok(chatRoomDto);
    }//10.17

    @GetMapping("/chat/rooms")
    public ResponseEntity<List<ChatRoomDto>> getUserChatRooms(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 로그인된 사용자의 이메일을 기반으로 회원 정보 조회
        String userEmail = principal.getName();
        Member member = userService.findByEmail(userEmail);

        // 해당 사용자가 참여 중인 채팅방 리스트 조회
        List<ChatRoom> chatRooms = chatRoomService.getChatRoomList(member.getId());

        // ChatRoom 엔티티 리스트를 DTO 리스트로 변환
        List<ChatRoomDto> chatRoomDtos = chatRooms.stream()
                .map(ChatRoomDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatRoomDtos);
    }

//    @PostMapping("/getUserNumber")
//    @ResponseBody
//    public Long getUserNumber(HttpSession session, @ModelAttribute("userNumber")Long userNumber, Model model){
//
//        Member user = (Member) session.getAttribute("user");
//        System.out.println(user);
//
////        if(user == null) {
////            User user = userService.getUserByNumber(userNumber);
////            Long userNumber = Long.valueOf(-1);
////        }
////
//        if(userNumber == 2){
//            model.addAttribute("user2",user);
//        }else if(userNumber == 1){
//            model.addAttribute("user1",user);
//        }else if(userNumber == 3){
//            model.addAttribute("user3",user);
//        }else if(userNumber == 4){
//            model.addAttribute("user4",user);
//        }
//
//
//        if(user != null){
//            userNumber = user.getId();  // getUserNumber ****
//        }
//        System.out.println("userNumber입니다" + userNumber);
//
//        return userNumber;
//    }
//
//
//
//    @GetMapping("addSession2")
//    public String addSession2(Model model,HttpSession session){
//        List<ChatRoom> roomList = null;
//
//
//        Member user2 = (Member) session.getAttribute("user2");
//        Member user3 = (Member) session.getAttribute("user3");
//        List<Member> userList = new ArrayList<>();
//
//        if(user2 !=null){
//            userList.add(user2);
//
//        }else if(user3 != null){
//            userList.add(user3);
//
//        }
//
//        Member user = new Member();
//        long userNumber = 2;
//
//        user = userService.getUserByNumber(userNumber);
//        model.addAttribute("user2",user);
//
//        return "community/home";
//
//    }
//
//
//    @PostMapping("/getMessageList")
//    @ResponseBody
//    public Map<String,Object> getMessageList(@ModelAttribute("roomId")Long roomId){
//
//
//        Map<String,Object> chatRoomInfo = new HashMap<>();
//
//        List<ChatMessage> chatMessageList = chatMessageService.getAllMessage(roomId);
//        ChatRoom chatRoom = chatRoomService.getChatRoomByRoomId(roomId);
//
//        chatRoomInfo.put("chatRoom",chatRoom);
//        chatRoomInfo.put("chatMessageList",chatMessageList);
//
//        return chatRoomInfo;
//    }
//
//
//    @RequestMapping("/chatTest1")
//    public String chatTest2(Model model, HttpSession session){
//
//
//        Member user = new Member();
//        long userNumber = 1;
//
//        user = userService.getUserByNumber(userNumber);
//        model.addAttribute("user1",user);
//
//        return "community/home";
//
//    }
//
//
//    @RequestMapping("/chatTest2")
//    public String chatTest1(Model model, HttpSession session){
//
//
//        Member user = new Member();
//        long userNumber = 2;
//
//        user = userService.getUserByNumber(userNumber);
//        model.addAttribute("user2",user);
//
//        return "community/home";
//
//    }
//
//    @RequestMapping("/chatTest3")
//    public String chatTest3(Model model, HttpSession session){
//
//
//        Member user = new Member();
//        long userNumber = 3;
//
//        user = userService.getUserByNumber(userNumber);
//        model.addAttribute("user3",user);
//
//        return "community/home";
//
//    }
//
//
//    @RequestMapping("/chatTest4")
//    public String chatTest4(Model model, HttpSession session){
//
//
//        Member user = new Member();
//        long userNumber = 4;
//
//        user = userService.getUserByNumber(userNumber);
//        model.addAttribute("user4",user);
//
//        return "community/home";
//
//    }
//
//
//    @PostMapping("/getChatData")
//    @ResponseBody
//    public Map<String,Object> getChatData(@ModelAttribute("userNumber")Long userNumber){
//
//
//        System.out.println("userNUmber : " + userNumber);
//
//
//
//        List<ChatRoom> roomList = null;
//        List<String> lastMessageList = null;
//        Map<String, Object> userInfo = new HashMap<>();
//
//
//        // null 일수 있기 때문에 session 객체로 뽑아낸다
//        Member user = userService.getUserByNumber(userNumber);
//
//        if(user != null) {
//
//            userNumber = user.getId();
//            roomList = chatRoomService.getChatRoomList(userNumber);
//            lastMessageList = chatMessageService.getLastMessageList(userNumber,roomList);
//
//        }
//
//
//        // 채팅방을 열게되면 채팅방 목록에 해당하는 마지막 메시지 리스트를 받아와야한다.
//        // 여기도 바꿔야하낟. 테스트하려면.
//        userInfo.put("user",user);
//        userInfo.put("roomList",roomList); // roomList객체를 반환한다.
//        userInfo.put("lastMessageList",lastMessageList);
//        return userInfo;
//
//
//    }
//
//    @ResponseBody
//    @RequestMapping("/addChatRoom")
//    public void addChatRoom(@ModelAttribute("writerNumber")Long writerNumber,
//                            @ModelAttribute("userNumber")Long userNumber){
//
//
//        chatRoomService.addChatRoom(writerNumber, userNumber);
//
//    }
}
