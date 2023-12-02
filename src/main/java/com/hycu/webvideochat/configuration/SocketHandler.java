package com.hycu.webvideochat.configuration;

import com.hycu.webvideochat.service.ChatRoomManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SocketHandler extends TextWebSocketHandler {

    @Autowired
    ChatRoomManageService chatRoomManageService ;

    //id - 세션id value - room id
    //연속해서 들어올 상황을 대비해 Thread Safe
    Map<String, List<WebSocketSession>> usersOfRoom = new Hashtable<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = chatRoomManageService.getRoomId();
        try {
            if (usersOfRoom.get(roomId) != null) {
                usersOfRoom.get(roomId).add(session);
            } else {
                List<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();
                sessionList.add(session);
                usersOfRoom.put(roomId, sessionList);
            }
            log.info(session.getId() + " - 연결됨");
        }finally {
            roomId = "";            //사용후 비우기
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String key = findKeyByWebSocketSession(usersOfRoom, session);
        if(key == null){
            log.info(session.getId() + " - 못찾고 끊어짐");
            return;
        }
        usersOfRoom.get(key).remove(session);
        if(usersOfRoom.get(key).size() <= 0){       //유저가 모두 끊긴경우 채팅방 삭제
            usersOfRoom.remove(key);
        }
        log.info(session.getId() + " - 끊어짐");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("(받은 메세지)" + session.getId()+" : " + message);
        for (WebSocketSession webSocketSession : usersOfRoom.get(findKeyByWebSocketSession(usersOfRoom, session))) {            //같은 채팅방 유저에게만 보낸다
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message);
                log.info("(보내는메세지)" + webSocketSession.getId()+" : " + message);
            }
        }
    }

    public String findKeyByWebSocketSession(Map<String, List<WebSocketSession>> map, WebSocketSession targetSession) {
        // 원하는 WebSocketSession을 가진 key를 찾는 스트림 작업
        Optional<String> foundKey = map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(targetSession))
                .map(Map.Entry::getKey)
                .findFirst();

        return foundKey.orElse(null);
    }

}
