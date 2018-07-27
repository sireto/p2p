package com.soriole.kademlia.core.network;

public class ServerShutdownException extends Exception {
    public ServerShutdownException() {
    }

    public ServerShutdownException(String reason) {
        super(reason);
    }
}
