package com.marketshop.marketshop.entity;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class ChatRoomForm {

    private Long roomId;
    private Member user1;
    private Member user2;


    public ChatRoom toEntity() {

        return ChatRoom.builder().
                roomId(roomId).
                user1(user1).
                user2(user2).build();

    }
}
