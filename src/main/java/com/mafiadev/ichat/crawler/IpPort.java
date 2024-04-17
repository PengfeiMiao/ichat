package com.mafiadev.ichat.crawler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Data
@AllArgsConstructor
public class IpPort {
    private String ip;

    private Integer port;

    public Proxy toProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
    }
}
