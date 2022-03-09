package com.bsuir.spolksfive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pcap4j.packet.Packet;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PingResponse {
    private UUID uuid;
    private long sendTime;
    private long receiveTime;
    private long ttl;
    private Packet packet;

    public long getPing() {
        return receiveTime - sendTime;
    }
}
