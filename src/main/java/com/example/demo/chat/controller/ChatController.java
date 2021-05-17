package com.example.demo.chat.controller;

import com.example.demo.chat.model.ChatMessage;
import com.example.demo.chat.repository.ChatRepository;
import com.example.demo.chat.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final PublisherService redisPublisher;
    private final ChatRepository chatRepository;

    /**
     * @MessageMapping("url") : "url"으로 들어오는 메시지 매핑할때 사용하는 애노테이션
     * "/pub/chat/message" 으로 들어오는 message를 ChatMessage으로 바인딩하여 실행
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        /** Message Type이 Enter이면 입장 **/
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            /** 1. 방 번호로 채팅방을 생성 **/
            chatRepository.enterChatRoom(message.getRoomId());
            /** 2. 객체에 입장한다는 메세지 담기 **/
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }

        /** 1. 생성된 방 목록에서 roomId에 해당하는 방 가져와서 **/
        ChannelTopic topic = chatRepository.getTopic(message.getRoomId());
        /** 2. 해당 방에 message 전달하기 **/
        redisPublisher.publish(topic, message);
    }
}
