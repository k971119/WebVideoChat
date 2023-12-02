package com.hycu.webvideochat.dto;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

@Data
public class RoomDTO {
    String id;
    String title;
    String password;
    String content;

    Set<WebSocketSession> chatUsers;
}
