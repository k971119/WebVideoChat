package com.hycu.webvideochat.dto;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Data
public class RoomDTO {
    String id;
    String title;
    String password;
    String content;

    List<WebSocketSession> chatUsers;
}
