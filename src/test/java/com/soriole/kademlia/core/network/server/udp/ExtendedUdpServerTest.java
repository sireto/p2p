package com.soriole.kademlia.core.network.server.udp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;

public class ExtendedUdpServerTest {

    ExecutorService service = Executors.newFixedThreadPool(10);
    int echoServerport = 8080;
    int localServerport = 8080;

    @Test
    public void testConnectivity() throws IOException {
        // create a echo server on  port 8080
        startEcho();

        // create a socket to connect the echo server using the same port used by localServer
        Socket socket = new Socket();
        // but this will throw SocketBindException
        socket.connect(new InetSocketAddress(echoServerport));

        // write hello
        socket.getOutputStream().write("Hello !".getBytes());
        socket.getOutputStream().flush();
        byte[] result = new byte[100];

        // receive hello
        String ans = new String(result, 0, socket.getInputStream().read(result));
        System.out.println("Server replied with : " + ans);

        // what was written and what was received must be same.
        assert (ans.equals("Hello !"));

    }

    // start a echo server listening on the specified port
    private void startEcho() throws IOException {
        ServerSocket echoServer = new ServerSocket(echoServerport);
        service.submit(() -> {
            try {
                while (!echoServer.isClosed()) {
                    Socket socket = echoServer.accept();
                    System.out.println("connected with :" + socket.getInetAddress().toString() + ":" + socket.getPort());

                    InputStream inputStream = socket.getInputStream();
                    OutputStream outputStream = socket.getOutputStream();

                    service.submit(() -> {
                        while (socket.isConnected()) {
                            try {
                                byte[] buffer = new byte[1024];
                                int read = -1;
                                while ((read = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, read);
                                }
                            } catch (IOException e) {
                                break;
                            }
                        }
                        System.out.println("The Client has closed connection.");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread.yield();

    }
// Write something to the socket.
}