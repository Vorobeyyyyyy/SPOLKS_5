package com.bsuir.spolksfive.service;

import com.bsuir.spolksfive.config.Properties;
import com.bsuir.spolksfive.dto.MessageDto;
import com.bsuir.spolksfive.factory.PacketFactory;
import com.bsuir.spolksfive.factory.PcapHandleFactory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.Inet4Address;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@NoArgsConstructor
@Slf4j
public abstract class ChatService {

    protected PacketFactory packetFactory;
    protected PcapHandleFactory pcapHandleFactory;
    protected Properties properties;

    public void startMessaging() {
        Future<?> messageListener = null;
        try (PcapHandle pcapHandle = pcapHandleFactory.createPcapHandle()) {
            Scanner scanner = new Scanner(System.in);
            messageListener = startListener(pcapHandle);
            String message;
            do {
                MessageDto messageDto = MessageDto.builder()
                        .sender(properties.getChatName())
                        .message(message = scanner.nextLine())
                        .build();
                writeToConsole(messageDto);
                Packet packet = packetFactory.buildMessagePacket(getDestinationAddress(), messageDto);
                pcapHandle.sendPacket(packet);
            } while (!message.equals("exit"));
            pcapHandle.breakLoop();
        } catch (NotOpenException | PcapNativeException e) {
            log.error("Error in messaging: ", e);
        } finally {
            if (messageListener != null) {
                messageListener.cancel(true);
            }
        }
    }

    private Future<?> startListener(PcapHandle pcapHandle) {
        return Executors.newSingleThreadExecutor().submit(() -> {
            try {
                try {
                    pcapHandle.loop(-1, (PacketListener) packet ->
                            packetFactory.toMessage(packet).ifPresent(this::writeToConsole));
                } catch (InterruptedException exception) {
                    log.info("Listener was interrupted");
                } catch (PcapNativeException e) {
                    log.error("Error while listening: ", e);
                }
            } catch (NotOpenException notOpenException) {
                log.error("Pcap handle was not open: ", notOpenException);
            }
        });
    }

    protected synchronized void writeToConsole(MessageDto dto) {
        System.out.printf("%s: %s%n", dto.getSender(), dto.getMessage());
    }

    protected abstract Inet4Address getDestinationAddress();

    @Autowired
    public void setPacketFactory(PacketFactory packetFactory) {
        this.packetFactory = packetFactory;
    }

    @Autowired
    public void setPcapHandleFactory(PcapHandleFactory pcapHandleFactory) {
        this.pcapHandleFactory = pcapHandleFactory;
    }

    @Autowired
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
