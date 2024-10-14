package com.marketshop.marketshop.dto;

import com.marketshop.marketshop.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {

    private Long roomId;
    private Long user1Id;
    private String user1Name;
    private Long user2Id;
    private String user2Name;
    private Long itemId;

    // ChatRoom 엔티티를 DTO로 변환하는 메서드
    public static ChatRoomDto fromEntity(ChatRoom chatRoom) {
        return new ChatRoomDto(
                chatRoom.getRoomId(),
                chatRoom.getUser1().getId(),
                chatRoom.getUser1().getName(),
                chatRoom.getUser2().getId(),
                chatRoom.getUser2().getName(),
                chatRoom.getItem().getId()
        );
    }
}
