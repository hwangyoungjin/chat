package com.example.demo;

import com.example.demo.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final ChatRepository chatRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Room 객체 만들고
        chatRepository.createChatRoom("test1");
    }
}
