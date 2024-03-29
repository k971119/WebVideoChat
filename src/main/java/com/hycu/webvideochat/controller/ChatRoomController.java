package com.hycu.webvideochat.controller;

import com.hycu.webvideochat.configuration.SocketHandler;
import com.hycu.webvideochat.dto.RoomDTO;
import com.hycu.webvideochat.service.ChatRoomManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ChatRoomController {

    @Autowired
    ChatRoomManageService chatRoomManageService;

    @Autowired
    SocketHandler socketHandler;

    @Autowired
    JoinController joinController;

    @GetMapping("/create/room")
    public String createRoom(Model model, RoomDTO roomDTO){

        /*//테스트용
        if(roomDTO.getTitle().isEmpty()) {
            roomDTO.setTitle("테스트 채팅방");
            roomDTO.setPassword("1234");
        }*/

        String roomId = chatRoomManageService.createRoom(roomDTO);

        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", roomDTO.getTitle());

        return "chatRoom";
    }

    /**
     * 매칭되는 채팅방 입장
     * @param model
     * @param roomId
     * @param password
     * @return
     */
    @GetMapping("/enter/room/{roomId}/{password}")
    public String enterChatRoom(Model model, @PathVariable("roomId") String roomId, @PathVariable("password") String password){
        RoomDTO roomDTO = chatRoomManageService.getRoomInfo(roomId);
        if(roomDTO.getPassword().equals(password)) {
            if (roomDTO != null) {
                model.addAttribute("roomId", roomDTO.getId());
                model.addAttribute("roomName", roomDTO.getTitle());
            } else {
                joinController.login(model);
            }
            return "chatRoom";
        }
        return "error/login";
    }

    /*@GetMapping("/socket/{roomId}")
    public String connectWebSocket(@PathVariable("roomId") String roomId){

        socketHandler.setRoomId(roomId);

        return "socket";
    }*/

}
