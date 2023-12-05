package com.hycu.webvideochat.configuration;

import com.hycu.webvideochat.dto.RoomDTO;
import com.hycu.webvideochat.service.ChatRoomManageService;
import lombok.Getter;
import lombok.Setter;
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

    //핸드쉐이크 인터셉터가 roomID를 담아놓음(Thread Safe를 위해서 사용)
    @Getter
    private final ThreadLocal<String> roomIdThreadLocal = new ThreadLocal<>();



    /**
     * 웹소켓 연결 요청전 처리
     * @param session   연결요청한 세션
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //핸드쉐이크 인터셉터단에서 가져온 RoomID
        String roomId = roomIdThreadLocal.get();
        RoomDTO roomInfo = chatRoomManageService.getRoomInfo(roomId);
        try {
            if (roomInfo.getChatUsers() != null) {
                roomInfo.getChatUsers().add(session);
            } else {
                List<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();
                sessionList.add(session);
                roomInfo.setChatUsers(sessionList);
            }
            log.info(session.getId() + " - 연결됨");
        }finally {
            roomId = "";            //사용후 비우기
        }
    }

    /**
     * 웹 소켓 연결 해제전 처리
     * @param session   연결해제한 소켓 세션
     * @param status    상태값
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //닫힐때는 service단의 roomId가 다른채팅방에 진입할경우 변조가능성이 있기 때문에 session값을 통해 roomId를 찾아온다
        String key = getRoomIdByWebSocketSession(chatRoomManageService.getChatRooms() ,session);
        RoomDTO roomInfo = chatRoomManageService.getRoomInfo(key);
        if(key == null){
            log.info(session.getId() + " 해당 세션이 포함된 채팅방을 찾지못함");
            return;
        }
        roomInfo.getChatUsers().remove(session);
        if(roomInfo.getChatUsers().size() <= 0){       //유저가 모두 끊긴경우 채팅방 삭제
            chatRoomManageService.destroyChatRoom(key);
        }
        log.info(session.getId() + " - 끊어짐");
    }

    /**
     * 웹소켓이 전송한 메시지 같은채팅방 유저에게 전달
     * @param session   메시지를 전송한 세션
     * @param message   받은 메세지
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("(받은 메세지)" + session.getId()+" : " + message);
        for (WebSocketSession webSocketSession :
                chatRoomManageService.getRoomInfo(getRoomIdByWebSocketSession(chatRoomManageService.getChatRooms(),session)).getChatUsers()) {            //같은 채팅방 유저에게만 보낸다(N:N으로 업그레이드를 고려해서 이렇게 만듬...)
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message);
                log.info("(보내는메세지)" + webSocketSession.getId()+" : " + message);
            }
        }
    }

    public String getRoomIdByWebSocketSession(Map<String, RoomDTO> rooms, WebSocketSession session) {
        for (Map.Entry<String, RoomDTO> entry : rooms.entrySet()) {
            RoomDTO roomDTO = entry.getValue();
            if (roomDTO.getChatUsers().contains(session)) {
                return roomDTO.getId();
            }
        }
        return null; // 해당하는 RoomDTO를 찾지 못한 경우
    }

}
