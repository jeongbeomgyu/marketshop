package com.marketshop.marketshop.dto;

import com.marketshop.marketshop.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String content;
    private Long roomId;

    // ChatMessage 엔티티를 DTO로 변환
    public ChatMessageDto(ChatMessage chatMessage) {
        this.messageId = chatMessage.getMessageId();
        this.senderId = chatMessage.getUser().getId();
        this.senderName = chatMessage.getUser().getName();
        this.content = chatMessage.getContent();
        this.roomId = chatMessage.getChatRoom().getRoomId();
    }
}
