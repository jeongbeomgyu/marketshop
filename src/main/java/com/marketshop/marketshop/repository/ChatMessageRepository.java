package com.marketshop.marketshop.repository;

import com.marketshop.marketshop.entity.ChatMessage;
import com.marketshop.marketshop.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByChatRoomOrderBySendTimeAsc(ChatRoom chatRoom);

    // 조회되는 채팅방과 같은 roomId를 가지는 메서드이면서 동시에 가장 최근의 메시지
    @Query("select m.content from ChatMessage m where m.chatRoom.roomId = :room_id order by m.sendTime desc")
    Page<String> selectLastMessageByChatRoom(@Param("room_id")Long room_id, Pageable pageable);

}
