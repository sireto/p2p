package com.soriole.kademlia.network.udp;

import com.soriole.kademlia.network.ByteResponseHandler;
import com.soriole.kademlia.network.ByteSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Implementation of {@link ByteSender} which sends messages in a
 * blocking way.
 */

public class UdpSocketByteSender implements ByteSender {
    public static final int INT_LENGTH = 4;
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpSocketByteSender.class);

    /**
     * Send given message. This method blocks till the sending operation is
     * finished.
     */
    @Override
    public void sendMessageWithReply(InetSocketAddress dest,
                                     byte[] message,
                                     ByteResponseHandler handler) {
        LOGGER.debug("sendMessageWithReply({}, {}, {})", dest, message.length, handler);

        byte[] answer = null;
        boolean hasSent = false;

        try (DatagramSocket socket = new DatagramSocket()){
            DatagramPacket packet = new DatagramPacket(message, message.length, dest);
            socket.send(packet);
        } catch (SocketException e) {
            LOGGER.error("Failed to send packet", e);
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to send packet", e);
        } catch (IOException e) {
            LOGGER.error("Failed to send packet", e);
        }

        try (Socket socket = new Socket(dest.getAddress(), dest.getPort())) {
            sendPacket(dest, message);
            hasSent = true;
            LOGGER.debug("sendMessageWithReply() -> sent successfully message of length: {}",
                    message.length);
            handler.onSendSuccessful();

            answer = readMessage(socket.getInputStream());
        } catch (IOException e) {
            if (hasSent) {
                handler.onResponseError(e);
            } else {
                handler.onSendError(e);
            }
            return;
        }
        if (answer == null) {
            LOGGER.debug("sendMessageWithReply() -> handler.onResponseError()");
            handler.onResponseError(new EOFException("Could not get response message."));
        } else {
            LOGGER.debug("sendMessageWithReply() -> handler.onResponse(length: {})", answer.length);
            handler.onResponse(answer);
        }
    }

    public void sendPacket(InetSocketAddress socketAddress, byte[] data){
        LOGGER.debug("Sending packet[len:%d] to %s", data.length, socketAddress);
        try (DatagramSocket socket = new DatagramSocket()){
            DatagramPacket packet = new DatagramPacket(data, data.length, socketAddress);
            socket.send(packet);
        } catch (SocketException e) {
            LOGGER.error("Failed to send packet", e);
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to send packet", e);
        } catch (IOException e) {
            LOGGER.error("Failed to send packet", e);
        }

    }

    static byte[] readBytes(InputStream is, int byteCnt) throws IOException {
        int remaining = byteCnt;
        byte[] array = new byte[byteCnt];
        while (remaining != 0) {
            int len = is.read(array, byteCnt - remaining, remaining);
            if (len < 1) {
                return null;
            }
            remaining -= len;
        }
        return array;
    }

    static byte[] readMessage(InputStream is) throws IOException {
        byte[] intArray = readBytes(is, INT_LENGTH);
        if (intArray == null) {
            return null;
        }
        int msgLength = transformByteArrayToInt(intArray);

        byte[] msgArray = readBytes(is, msgLength);
        return msgArray;
    }

    static void writeMessage(OutputStream outputStream, byte[] message) throws IOException {
        outputStream.write(transformIntToByteArray(message.length));
        outputStream.write(message);
    }

    private static int transformByteArrayToInt(byte[] array) {
        return ByteBuffer.wrap(array).asIntBuffer().get();
    }

    private static byte[] transformIntToByteArray(int length) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(INT_LENGTH);
        byteBuffer.putInt(length);
        return byteBuffer.array();
    }
}
