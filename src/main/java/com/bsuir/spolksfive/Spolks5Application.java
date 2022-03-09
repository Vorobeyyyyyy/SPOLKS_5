package com.bsuir.spolksfive;

import com.bsuir.spolksfive.config.Properties;
import com.bsuir.spolksfive.service.BroadcastChatService;
import com.bsuir.spolksfive.service.MulticastChatService;
import com.bsuir.spolksfive.service.PingService;
import com.bsuir.spolksfive.service.TracerouteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;

@SpringBootApplication
@Slf4j
@AllArgsConstructor
public class Spolks5Application implements CommandLineRunner {

    private PingService pingService;
    private TracerouteService tracerouteService;
    private BroadcastChatService broadcastChatService;
    private MulticastChatService multicastChatService;
    private Properties properties;

    public static void main(String[] args) {
        SpringApplication.run(Spolks5Application.class, args);
    }


    @Override
    public void run(String... args) {
        switch (properties.getTask()) {
            case PING -> properties.getPingAddresses().forEach(address ->
                    pingService.pingTimesAsync(address,
                            properties.getPingRepeatCount(),
                            properties.getPingTtl(),
                            Duration.ofSeconds(1)));
            case TRACEROUTE -> tracerouteService.traceroute(properties.getTracerouteAddress(), properties.getTracerouteMaxHops());
            case BROADCAST_CHAT -> broadcastChatService.startMessaging();
            case MULTICAST_CHAT -> multicastChatService.startMessaging();
        }
        log.info("Application closed");
    }
}
