package com.example.demo.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 관련 설정
     * configureMessageBroker에서 Application 내부에서 사용할 path를 지정 할 수 있다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /** setApplicationDestinationPrefixes : client에서 SEND 요청을 처리한다. **/
        /*
        Spring Reference에서는 /topic, /queue가 주로 등장하는데 여기서는 이해를 돕기 위해 /pub 으로 지정하였다.
         /topic : 암시적으로 1:N 전파를 의미한다.
         /queue : 암시적으로 1:1 전파를 의미한다.
         */
        registry.setApplicationDestinationPrefixes("/pub");

        /** 해당 경로로 SimpleBroker를 등록한다.
         * SimpleBroker는 해당하는 경로를 SUBSCRIBE하는 client에게
         * 메시지를 전달하는 간단한 작업을 수행한다 **/
        registry.enableSimpleBroker("/sub");


        /*
        + enableStompBrokerRelay : SimpleBroker의 기능과
        외부 message broker(RabbitMQ, ActiveMQ 등)에 메시지를 전달하는 기능을 가지고 있다.
         */
    }


    /**
     *  handshake와 통신을 담당할 endpoint를 지정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
    }
}


