package com.hycu.webvideochat.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 웹소켓 설정
 */
@Configuration
@EnableWebSocketMessageBroker
public class webSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        //전송시간 버퍼크기
        registry.setSendTimeLimit(15 * 1000).setSendBufferSizeLimit(512 * 1024);
        // 전송받고 보낼 메시지 사이즈
        registry.setMessageSizeLimit(128 * 1024);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //구독주소
        registry.enableSimpleBroker("/topic")
                .setTaskScheduler(taskScheduler())
                .setHeartbeatValue(new long[] {3000L, 3000L});
        //수신주소
        registry.setApplicationDestinationPrefixes("/app");
    }

    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 8);
        container.setMaxBinaryMessageBufferSize(1024 * 8);
        return container;
    }
}
