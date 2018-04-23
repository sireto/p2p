package com.soriole.kademlia.network.server;


import java.io.IOException;
import java.net.*;

/**
 * DataGram Server is responsible for sending and receiving Datagram raw packets from a DatagramSocket.
 */
public class DataGramServer {
    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;      // 64KB
    boolean active;
    protected DatagramSocket socket;

    protected DataGramServer(DatagramSocket socket){
        this.socket=socket;
    }
    protected synchronized DatagramPacket receivePacket() throws IOException {
        byte[] buffer = new byte[DATAGRAM_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;

    }

    protected void sendPacket(DatagramPacket packet) throws IOException {
        socket.send(packet);

    }

    protected void sendByteTo(byte[] data, InetSocketAddress address) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(address);
        sendPacket(packet);
    }

    protected DatagramPacket createPacket(byte[] data, InetSocketAddress address) {
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(address);
        return packet;
    }
    public final InetSocketAddress getSocketAddress(){
        return (InetSocketAddress) this.socket.getLocalSocketAddress();
    }
}
