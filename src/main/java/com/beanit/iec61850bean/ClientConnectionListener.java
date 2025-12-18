package com.beanit.iec61850bean;

import java.net.InetAddress;

/**
 * 服务端有新的client socket连接时发布消息
 */
public interface ClientConnectionListener {

    void newClientConnection(InetAddress address, int port);

    void remoteClinetStopConnect(InetAddress address, int port);
}
