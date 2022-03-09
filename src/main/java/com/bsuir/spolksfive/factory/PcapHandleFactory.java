package com.bsuir.spolksfive.factory;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PcapHandleFactory {

    private PcapNetworkInterface pcapNetworkInterface;

    @SneakyThrows(PcapNativeException.class)
    public PcapHandle createPcapHandle() {
        return new PcapHandle.Builder(pcapNetworkInterface.getName())
                .promiscuousMode(PcapNetworkInterface.PromiscuousMode.PROMISCUOUS)
                .timeoutMillis(10)
                .immediateMode(true)
                .snaplen(65536)
                .build();
    }
}
