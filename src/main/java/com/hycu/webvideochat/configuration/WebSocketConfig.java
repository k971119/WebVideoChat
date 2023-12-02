package com.hycu.webvideochat.configuration;

import com.hycu.webvideochat.service.ChatRoomManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;


/**
 * 웹소켓 설정
 */
@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    ChatRoomManageService chatRoomManageService;

    @Autowired
    SocketHandler socketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/socket/{roodId}")
                .setAllowedOriginPatterns("*")
                .addInterceptors(getInter());
    }

    private HandshakeInterceptor getInter(){
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse,
                                           WebSocketHandler webSocketHandler,
                                           Map<String, Object> map) throws Exception {

                String path = serverHttpRequest.getURI().getPath();
                int index = path.indexOf("/socket");
                String id = path.substring(index+8);
                chatRoomManageService.setRoomId(id);
                log.info("ID : " + id);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

            }
        };
    }
}
