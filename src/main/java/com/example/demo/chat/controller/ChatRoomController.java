package com.example.demo.chat.controller;

import com.example.demo.chat.model.ChatRoom;
import com.example.demo.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRepository chatRoomRepository;

    /**
     * main Page 보여주기
     */
    @GetMapping("/room")
    public String rooms(Model model) {
        return "/chat/room";
    }

    /**
     * 현재 있는 모든 채팅방 반환
     */
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        return chatRoomRepository.findAllRoom();
    }

    /**
     * 채팅 룸 생성
     */
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        return chatRoomRepository.createChatRoom(name);
    }

    /**
     * 채팅방 Page 리턴
     */
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    /**
     * 채팅방 번호로 찾기
     */
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }
}