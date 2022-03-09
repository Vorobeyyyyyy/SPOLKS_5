package com.bsuir.spolksfive.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.Packet;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class MulticastChatService extends ChatService {

    @Override
    public void startMessaging() {
        ScheduledExecutorService scheduledExecutorService = connectToMulticastGroup();
        log.info("Started multicast messaging");
        super.startMessaging();
        scheduledExecutorService.shutdownNow();
        log.info("Stopped multicast messaging");
    }

    private ScheduledExecutorService connectToMulticastGroup() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try (PcapHandle pcapHandle = pcapHandleFactory.createPcapHandle()) {
                Packet packet = packetFactory.buildIgmpJoinPacket(getDestinationAddress());
                pcapHandle.sendPacket(packet);
                log.info("Sent IGMP join packet");
            } catch (NotOpenException | PcapNativeException e) {
                log.error("Can not join multicast group: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }, 0, 30, TimeUnit.SECONDS);
        return scheduledExecutorService;
    }

    @Override
    protected Inet4Address getDestinationAddress() {
        return properties.getChatMulticastAddress();
    }
}
