package com.example.demo.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom implements Serializable {

    private Long id;

    private String roomId;

    private String name;

    private Long manId;

    private Long womanId;
}