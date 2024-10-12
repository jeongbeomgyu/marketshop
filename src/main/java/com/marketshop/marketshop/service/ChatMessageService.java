package com.marketshop.marketshop.service;

import com.marketshop.marketshop.entity.ChatMessage;
import com.marketshop.marketshop.entity.ChatRoom;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.repository.ChatMessageRepository;
import com.marketshop.marketshop.repository.ChatRoomRepository;
import com.marketshop.marketshop.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    MemberRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    public ChatMessage saveMessage(Long roomId, Long senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("ChatRoom not found"));
        Member sender = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        ChatMessage chatMessage = new ChatMessage(chatRoom, sender, content, LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getAllMessagesInRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found"));
        return chatMessageRepository.findAllByChatRoomOrderBySendTimeAsc(chatRoom);
    }

    public List<ChatMessage> getAllMessage(Long roomId){

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        List<ChatMessage> chatMessageList = chatMessageRepository.findAllByChatRoomOrderBySendTimeAsc(chatRoom);

        return chatMessageList;
    }

    public List<String> getLastMessageList(Long userNumber, List<ChatRoom> chatRoom){

        List<String> messageList = new ArrayList<>();
        Pageable pageable = PageRequest.of(0,1, Sort.by("sendTime").descending());
        String lastMessage = "";
        for(int i = 0 ; i < chatRoom.size(); i++) {
            Page<String> pageList = chatMessageRepository.selectLastMessageByChatRoom(chatRoom.get(i).getRoomId(), pageable);
            System.out.println(pageList.getContent());
            if(pageList.getContent().size() != 0){
                lastMessage = pageList.getContent().get(0);
            }else{
                lastMessage = "대화내용이 없습니다.";
            }
            messageList.add(lastMessage);
        }

        System.out.println(messageList);
        return messageList;
    }

}
