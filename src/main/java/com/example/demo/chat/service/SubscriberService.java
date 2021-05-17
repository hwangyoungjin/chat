package com.example.demo.chat.service;

import com.example.demo.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriberService implements MessageListener {

    private final RedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    /** SimpMessageSendingOperations는 어디서든지 message를 보낼 수 있는 클래스**/
    private final SimpMessageSendingOperations messageTemplate;

    /**
     *  Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try{
            /**redis에 publish 된 데이터 받아서 deserialize**/
            String publishedMessge = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            /**deserialize 한 메시지를 내가 만든 ChatMessage 객체로 맵핑**/
            ChatMessage chatMessage = objectMapper.readValue(publishedMessge, ChatMessage.class);

            /** SimpMessageSendingOperations 이용해서 Subscriber에게 메시지 보내기**/
            /** URL : '"/sub/chat/room/"+chatMessage.getRoomId()' 으로 chatMessage 보내기 **/
            messageTemplate.convertAndSend("/sub/chat/room/"+chatMessage.getRoomId(), chatMessage);
        } catch (Exception e){
            log.error(e.getMessage());
        }

    }
}
