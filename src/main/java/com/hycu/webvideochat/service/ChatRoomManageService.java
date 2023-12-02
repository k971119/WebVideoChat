package com.hycu.webvideochat.service;

import com.hycu.webvideochat.dto.RoomDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatRoomManageService {

    /**
     * 채팅룸의 정보를 담는 변수
     */
    @Getter
    private Map<String, RoomDTO> chatRooms = new HashMap<>();

    @Setter
    @Getter
    private String roomId;

    public String createRoom(RoomDTO roomDTO){
        //id생성
        String roomId = UUID.randomUUID().toString();
        roomDTO.setId(roomId);
        chatRooms.put(roomId, roomDTO);

        return roomId;
    }

    public void destroyChatRoom(String roomId){
        chatRooms.remove(roomId);
    }


    public void setChatRoom(String roomId, RoomDTO cahtRoom) {
        chatRooms.put(roomId, cahtRoom);
    }

    public RoomDTO getChatRoom(String roomId) {
        return chatRooms.get(roomId);
    }

    public List<RoomDTO> getRoomList(){
        return new ArrayList<>(chatRooms.values());
    }

    public RoomDTO getRoomInfo(String roomId) {
        return chatRooms.get(roomId);
    }
}
