package com.marketshop.marketshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// 기본키 값을 자동으로 생성한다.
    @Column(name = "message_id")       // 예슬 추가함(외래키)
    private Long messageId;


    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn( name = "room_id")
    private ChatRoom chatRoom;


    @Column
    private String content; // 메시지 내용

    @Column
    private Date sendTime; // 전송 시각

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "sender_id")
    private Member user; // 해당 메시지의 발신자  (테이블 컬럼명 : sender_id)

    // 원하는 필드를 받을 수 있는 생성자 추가
    public ChatMessage(ChatRoom chatRoom, Member user, String content, LocalDateTime sendTime) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.content = content;
        this.sendTime = java.sql.Timestamp.valueOf(sendTime); // LocalDateTime을 Date로 변환
    }

}
