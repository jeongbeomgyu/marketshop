package com.marketshop.marketshop.repository;

import com.marketshop.marketshop.entity.ChatRoom;
import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT r, MAX(m.sendTime) AS time " +
            "FROM ChatRoom r " +
            "LEFT JOIN ChatMessage m " +
            "ON r.roomId = m.chatRoom.roomId " +
            "WHERE r.user1.id = :userNumber OR r.user2.id= :userNumber " +
            "GROUP BY r.roomId " +
            "ORDER BY time DESC")
    List<ChatRoom> selectChatRoomByUserNumber(@Param("userNumber")Long userNumber);
    @Query("select r from ChatRoom r where (r.user1.id = :writerNumber and r.user2.id = :userNumber) " +
            "or (r.user1.id = :userNumber and r.user2.id =:writerNumber) ")
    ChatRoom findByUser1AndUser2(@Param("writerNumber")Long writerNumber,@Param("userNumber")Long userNumber);

    // 사용자 1 (판매자)와 사용자 2 (구매자) 그리고 상품(Item)으로 채팅방 찾기
    ChatRoom findByUser1AndUser2AndItem(Member user1, Member user2, Item item);



}
