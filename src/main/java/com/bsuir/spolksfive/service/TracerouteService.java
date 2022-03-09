package com.bsuir.spolksfive.service;

import com.bsuir.spolksfive.exception.PingTimeoutException;
import com.bsuir.spolksfive.exception.TimeExceededException;
import com.bsuir.spolksfive.factory.PcapHandleFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.PcapHandle;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.time.Duration;

@Service
@AllArgsConstructor
@Slf4j
public class TracerouteService {

    private PingService pingService;
    private PcapHandleFactory pcapHandleFactory;

    public void traceroute(Inet4Address inet4Address, int maxHops) {
        try (PcapHandle pcapHandle = pcapHandleFactory.createPcapHandle()) {
            int ttl = 1;
            while (ttl <= maxHops) {
                try {
                    pingService.ping(pcapHandle, inet4Address, (byte) ttl, Duration.ofSeconds(1));
                    log.info("{} hop: {}", ttl, inet4Address.getHostAddress());
                    break;
                } catch (TimeExceededException e) {
                    log.info("{} hop: {}", ttl, e.getLastNode().getHostAddress());
                } catch (PingTimeoutException e) {
                    log.info("{} hop: {}", ttl, "*");
                } catch (Throwable t) {
                    log.error("{} hop: {}", ttl, t.getMessage());
                } finally {
                    ttl++;
                }
            }
        }
    }
}
