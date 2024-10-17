package com.marketshop.marketshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Builder

public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;


    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn( name = "userNumber1")
    private Member user1;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn( name = "userNumber2")
    private Member user2;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @OneToMany(mappedBy = "chatRoom", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();

    // 채팅방 생성 메소드
    public static ChatRoom createChatRoom(Member user1, Member user2, Item item) {
        return ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .item(item)
                .build();
    }

    public boolean isParticipant(Member member) {
        return this.user1.equals(member) || this.user2.equals(member);
    }
}
