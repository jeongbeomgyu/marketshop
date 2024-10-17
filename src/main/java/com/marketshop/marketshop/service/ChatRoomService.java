package com.marketshop.marketshop.service;

import com.marketshop.marketshop.entity.ChatRoom;
import com.marketshop.marketshop.entity.ChatRoomForm;
import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.repository.ChatRoomRepository;
import com.marketshop.marketshop.repository.ItemRepository;
import com.marketshop.marketshop.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ChatRoomService {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MemberRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    // 채팅방 생성 또는 기존 방 가져오기
    public ChatRoom createOrGetChatRoom(Long itemId, Long user2Id) {
        // 아이템 조회 및 판매자 정보 가져오기
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        Member user1 = item.getMember(); // 상품을 등록한 판매자
        Member user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));

        // 판매자가 본인 아이템에 대해 채팅방을 생성하려고 할 경우 예외 발생
        if (user1.getId().equals(user2.getId())) {
            throw new IllegalArgumentException("You cannot create a chat room for your own item.");
        }

        // 채팅방이 이미 존재하는지 확인
        ChatRoom chatRoom = chatRoomRepository.findByUser1AndUser2AndItem(user1, user2, item);

        // 이미 존재하는 방이 없다면 생성
        if (chatRoom == null) {
            chatRoom = ChatRoom.createChatRoom(user1, user2, item);
            chatRoomRepository.save(chatRoom);
        }

        return chatRoom;
    }


    public List<ChatRoom> getChatRoomList(Long userNumber){


        return chatRoomRepository.selectChatRoomByUserNumber(userNumber);
    }

    public ChatRoom getChatRoomByRoomId(Long roomId){

        return chatRoomRepository.findById(roomId).orElse(null);
    }

    public void addChatRoom(Long writerNumber, Long userNumber){


        //이미 있는 방인지 없는 방인지도 검사해야한다.
        ChatRoom result = chatRoomRepository.findByUser1AndUser2(writerNumber,userNumber);
        if(result != null){ // 이미 생성된 대화방이 있었으면 대화방을 만들 필요가 없다.

            return;
        }

        log.info("작성자 {} 신청자 {}",writerNumber,userNumber);
        log.info("result값{}",result);

        Member writer = userRepository.findById(writerNumber).orElse(null);
        Member user = userRepository.findById(userNumber).orElse(null);

        ChatRoomForm chatRoomForm = new ChatRoomForm();
        chatRoomForm.setUser1(writer);
        chatRoomForm.setUser2(user);
        ChatRoom chatRoom = chatRoomForm.toEntity();
        chatRoomRepository.save(chatRoom);

    }
}
