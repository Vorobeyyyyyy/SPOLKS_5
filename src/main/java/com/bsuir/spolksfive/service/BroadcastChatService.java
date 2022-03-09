package com.bsuir.spolksfive.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;

@Service
@AllArgsConstructor
public class BroadcastChatService extends ChatService {

    @Override
    protected Inet4Address getDestinationAddress() {
        return properties.getChatBroadcastAddress();
    }
}
