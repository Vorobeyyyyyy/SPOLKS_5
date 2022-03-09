package com.bsuir.spolksfive.config;

import com.bsuir.spolksfive.factory.PcapHandleFactory;
import com.bsuir.spolksfive.service.PingService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.*;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.Inet4Address;

@Configuration
@Slf4j
@AllArgsConstructor
public class NetworkConfig {

    private Properties properties;

    @Bean
    public PcapNetworkInterface getPcapNetworkInterface() {
        try {
            if (properties.getDeviceAddress() != null) {
                return Pcaps.getDevByAddress(properties.getDeviceAddress());
            }
            return new NifSelector().selectNetworkInterface();
        } catch (IOException | PcapNativeException e) {
            throw new Error("Can't select network interface", e);
        }
    }

    @Bean
    public Inet4Address getInet4Address() {
        return (Inet4Address) getPcapNetworkInterface().getAddresses()
                .stream()
                .map(PcapAddress::getAddress)
                .filter(address -> address instanceof Inet4Address)
                .findAny()
                .orElseThrow(() -> new Error("Select network interface have no IPv4 address"));
    }

    @Bean("selfMacAddress")
    public MacAddress getMacAddress() {
        return (MacAddress) getPcapNetworkInterface().getLinkLayerAddresses()
                .stream()
                .filter(address -> address instanceof MacAddress)
                .findAny()
                .orElseThrow(() -> new Error("Select network interface have no MAC address"));
    }

    @Bean("nextHopMacAddress")
    public MacAddress getNextHopMacAddress() {
        return MacAddress.getByName(properties.getNextHopMacAddress());
    }

}
