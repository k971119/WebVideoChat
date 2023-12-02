package com.hycu.webvideochat.controller;

import com.hycu.webvideochat.service.ChatRoomManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JoinController {

    @Autowired
    ChatRoomManageService chatRoomManageService;

    @GetMapping("/")
    public String login(Model model){


        model.addAttribute("rooms",chatRoomManageService.getRoomList());

        return "login";
    }

}
