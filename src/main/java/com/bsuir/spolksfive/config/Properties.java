package com.bsuir.spolksfive.config;

import com.bsuir.spolksfive.enums.AppTask;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class Properties {
    private AppTask task;

    private String nextHopMacAddress;

    private List<Inet4Address> pingAddresses;
    private byte pingTtl;
    private int pingRepeatCount;

    private Inet4Address tracerouteAddress;
    private byte tracerouteMaxHops;

    private Inet4Address deviceAddress;

    private String chatMessagePrefix;
    private String chatName;
    private Inet4Address chatBroadcastAddress;
    private Inet4Address chatMulticastAddress;
}
