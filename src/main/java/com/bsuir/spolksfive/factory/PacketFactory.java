package com.bsuir.spolksfive.factory;

import com.bsuir.spolksfive.config.Properties;
import com.bsuir.spolksfive.dto.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Bytes;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.MacAddress;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class PacketFactory {

    private Inet4Address inet4Address;
    private MacAddress selfMacAddress;
    private MacAddress nextHopMacAddress;
    private Properties properties;
    private ObjectMapper objectMapper;

    public Packet buildPingPacket(Inet4Address address,
                                  byte ttl) {
        UUID packetUuid = UUID.randomUUID();

        IcmpV4EchoPacket.Builder echoBuilder = new IcmpV4EchoPacket.Builder();
        echoBuilder
                .identifier((short) packetUuid.getMostSignificantBits())
                .payloadBuilder(new UnknownPacket.Builder().rawData(ByteBuffer.allocate(24)
                        .putLong(System.currentTimeMillis())
                        .putLong(packetUuid.getMostSignificantBits())
                        .putLong(packetUuid.getLeastSignificantBits())
                        .array()));

        IcmpV4CommonPacket.Builder icmpV4CommonBuilder = new IcmpV4CommonPacket.Builder();
        icmpV4CommonBuilder
                .type(IcmpV4Type.ECHO)
                .code(IcmpV4Code.NO_CODE)
                .payloadBuilder(echoBuilder)
                .correctChecksumAtBuild(true);

        return buildIpV4Packet(address, ttl, icmpV4CommonBuilder, IpNumber.ICMPV4);
    }

    @SneakyThrows(JsonProcessingException.class)
    public Packet buildMessagePacket(Inet4Address address, MessageDto messageDto) {
        return buildIpV4Packet(address, (byte) 64, new UnknownPacket.Builder().rawData(
                ArrayUtils.addAll(
                        properties.getChatMessagePrefix().getBytes(),
                        objectMapper.writeValueAsBytes(messageDto))), IpNumber.getInstance((byte) 253));
    }

    public Packet buildIgmpJoinPacket(Inet4Address destinationAddress) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8)
//                .putShort((short) 1) // number of records
                .put(destinationAddress.getAddress());
        IcmpV4CommonPacket.Builder icmpV4CommonBuilder = new IcmpV4CommonPacket.Builder();
        icmpV4CommonBuilder
                .type(new IcmpV4Type((byte) 0x16, ""))
                .code(new IcmpV4Code((byte) 0, ""))
                .payloadBuilder(new UnknownPacket.Builder().rawData(byteBuffer.array()))
                .correctChecksumAtBuild(true);

        return buildIpV4Packet(destinationAddress, (byte) 1, icmpV4CommonBuilder, IpNumber.IGMP);
    }

    private EthernetPacket buildIpV4Packet(Inet4Address address,
                                           byte ttl,
                                           Packet.Builder icmpV4CommonBuilder, IpNumber ipNumer) {
        IpV4Packet.Builder ipV4Builder = new IpV4Packet.Builder();
        ipV4Builder
                .version(IpVersion.IPV4)
                .tos(IpV4Rfc791Tos.newInstance((byte) 0))
                .ttl(ttl)
                .protocol(ipNumer)
                .srcAddr(inet4Address)
                .dstAddr(address)
                .payloadBuilder(icmpV4CommonBuilder)
                .correctChecksumAtBuild(true)
                .correctLengthAtBuild(true);

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder
                .dstAddr(nextHopMacAddress)
                .srcAddr(selfMacAddress)
                .type(EtherType.IPV4)
                .payloadBuilder(ipV4Builder)
                .paddingAtBuild(true);

        return etherBuilder.build();
    }

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public Optional<MessageDto> toMessage(Packet packet) {
        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
        if (ipV4Packet == null) {
            return Optional.empty();
        }
        if (ipV4Packet.getHeader().getSrcAddr().equals(inet4Address)) {
            return Optional.empty();
        }
        byte[] data = ipV4Packet.getPayload().getRawData();
        byte[] prefixBytes = properties.getChatMessagePrefix().getBytes();
        if (Bytes.indexOf(data, prefixBytes) == 0) {
            return Optional.of(objectMapper.readValue(ArrayUtils.subarray(data, prefixBytes.length, data.length), MessageDto.class));
        }
        return Optional.empty();
    }
}
