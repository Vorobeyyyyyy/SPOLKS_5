package com.bsuir.spolksfive.service;

import com.bsuir.spolksfive.dto.PingResponse;
import com.bsuir.spolksfive.exception.PingTimeoutException;
import com.bsuir.spolksfive.exception.TimeExceededException;
import com.bsuir.spolksfive.factory.PacketFactory;
import com.bsuir.spolksfive.factory.PcapHandleFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PingService {

    private PcapHandleFactory pcapHandleFactory;
    private PacketFactory packetFactory;

    @SneakyThrows({PcapNativeException.class, NotOpenException.class})
    public PingResponse ping(PcapHandle pcapHandle,
                             Inet4Address dstAddress,
                             byte ttl,
                             Duration timeout) throws PingTimeoutException, TimeExceededException {
//        pcapHandle.setFilter("icmp and dst host " + Pcaps.toBpfString(inet4Address),
//                BpfProgram.BpfCompileMode.OPTIMIZE);
        Packet packet = packetFactory.buildPingPacket(dstAddress, ttl);
        pcapHandle.sendPacket(packet);
        return waitResponsePacket(pcapHandle, packet, timeout);
    }

    @SneakyThrows(InterruptedException.class)
    @Async
    public void pingTimesAsync(Inet4Address address,
                               int repeatCount,
                               byte ttl,
                               Duration timeout) {
        int i = 0;
        try (PcapHandle pcapHandle = pcapHandleFactory.createPcapHandle()) {
            while (i++ != repeatCount) {
                try {
                    PingResponse pingResponse = ping(pcapHandle, address, ttl, timeout);
                    log.info("Ping to {}: {} ms ttl: {}", address, pingResponse.getPing(), pingResponse.getTtl());
                } catch (PingTimeoutException e) {
                    log.info("Ping to {}: timeout after {} ms", address, timeout.toMillis());
                } catch (TimeExceededException e) {
                    log.info("Ping to {}: time exceeded at {}", address, e.getLastNode());
                } catch (RuntimeException e) {
                    log.error("Ping to {}: {}", address, e.getMessage());
                } finally {
                    Thread.sleep(1000);
                }
            }
        }
    }

    private PingResponse waitResponsePacket(PcapHandle pcapHandle,
                                            Packet requestPacket,
                                            Duration timeout) throws PingTimeoutException, NotOpenException, TimeExceededException {
        long spendTime = 0;
        IcmpV4EchoPacket echoRequestPacket = requestPacket.get(IcmpV4EchoPacket.class);
        while (spendTime < timeout.toMillis()) {
            long startTime = System.currentTimeMillis();
            Packet packet = pcapHandle.getNextPacket();
            if (packet != null) {
                IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                long endTime = System.currentTimeMillis();
                IcmpV4EchoReplyPacket replyPacket = packet.get(IcmpV4EchoReplyPacket.class);
                if (replyPacket != null && replyPacket.getPayload().equals(echoRequestPacket.getPayload())) {
                    PingResponse response = toPingResponseDto(replyPacket.getPayload().getRawData());
                    response.setReceiveTime(endTime);
                    response.setPacket(packet);
                    response.setTtl(ipV4Packet.getHeader().getTtl());
                    return response;
                } else {
                    if (packet.contains(IcmpV4TimeExceededPacket.class)) {
                        IcmpV4EchoPacket echoPacket = packet.get(IcmpV4EchoPacket.class);
                        if (echoPacket != null && echoPacket.getHeader().getIdentifier() == echoRequestPacket.getHeader().getIdentifier()) {
                            throw new TimeExceededException(ipV4Packet.getHeader().getSrcAddr());
                        }
                    }
                }
            }
            spendTime += System.currentTimeMillis() - startTime;
        }
        throw new PingTimeoutException();
    }

    private PingResponse toPingResponseDto(byte[] rawData) {
        if (rawData.length == 24) {
            ByteBuffer buffer = ByteBuffer.wrap(rawData);
            long sendTime = buffer.getLong();
            UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
            return PingResponse.builder()
                    .uuid(uuid)
                    .sendTime(sendTime)
                    .build();
        }
        throw new IllegalArgumentException("rawData length must be 24");
    }
}
