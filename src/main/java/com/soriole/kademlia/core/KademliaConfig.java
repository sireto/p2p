package com.soriole.kademlia.core;

public class KademliaConfig {

    // the maximum no of nodes that will be put in a single bucket.
    private int k;

    // the maximum no of concurrent connections with other nodes.
    private int alpha;

    // When we send a message to another node,
    // The maximum no of seconds to wait for reply.
    private int timeout;

    // The no of threads to use for kademlia operations.
    private int nWorkers;

    // The length of bits to be used as kademlia Key.
    private int keyLength;

    // The maximum time a node can stay in bucket without being contacted.
    // After this time has passed, the node needs to be pinged to make sure it's alive.
    private int nodeAutoPingTime;

    //The time after which a content in  kademlia Store needs to be refreshed.
    private int keyValueRefreshTime;

    //The default time for which a content is kept in the kademlia.
    private int keyValueExpiryTime;

    // The Port where the Kademlia server will listen to.
    private int kadeliaProtocolPort;

    // The Port where the kademlia http server will listen to.
    private int httpPort;

    public int getHttpPort() {
        return httpPort;
    }

    public int getK() {
        return k;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getnWorkers() {
        return nWorkers;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getNodeAutoPingTime() {
        return nodeAutoPingTime;
    }

    public int getKeyValueRefreshTime() {
        return keyValueRefreshTime;
    }

    public int getKeyValueExpiryTime() {
        return keyValueExpiryTime;
    }

    public int getKadeliaProtocolPort() {
        return kadeliaProtocolPort;
    }
    public static KademliaConfig getDefaultConfig(){
        return new Builder().build();
    }
    public static Builder newBuilder(){
        return new Builder();
    }

    // since the kademlia config is a read only class,
    // We use a builder to create a KademliaConfig instance.
    public static class Builder{
        int k=2;
        int alpha=3;
        int timeout=3;
        int nWorkers=10;
        int keyLength=160;
        int nodeAutoPingTime=30;
        int keyValueRefreshTime=60*60;
        int keyValueExpiryTime=24*60*60;
        int kadeliaProtocolPort=0;
        int httpPort=80;
        Builder(){}

        public KademliaConfig build(){
            return new KademliaConfig(){
                {
                    super.k = Builder.this.k;
                    super.alpha = Builder.this.alpha;
                    super.timeout = Builder.this.timeout;
                    super.nWorkers = Builder.this.nWorkers;
                    super.keyLength = Builder.this.keyLength;
                    super.nodeAutoPingTime = Builder.this.nodeAutoPingTime;
                    super.keyValueRefreshTime = Builder.this.keyValueRefreshTime;
                    super.keyValueExpiryTime = Builder.this.keyValueRefreshTime;
                    super.kadeliaProtocolPort = Builder.this.kadeliaProtocolPort;
                    super.httpPort = Builder.this.httpPort;
                }
            };
        }
        public void setK(int k) {
            this.k = k;
        }

        public void setAlpha(int alpha) {
            this.alpha = alpha;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public void setnWorkers(int nWorkers) {
            this.nWorkers = nWorkers;
        }

        public void setKeyLength(int keyLength) {
            this.keyLength = keyLength;
        }

        public void setNodeAutoPingTime(int nodeAutoPingTime) {
            this.nodeAutoPingTime = nodeAutoPingTime;
        }

        public void setKeyValueRefreshTime(int keyValueRefreshTime) {
            this.keyValueRefreshTime = keyValueRefreshTime;
        }

        public void setKeyValueExpiryTime(int keyValueExpiryTime) {
            this.keyValueExpiryTime = keyValueExpiryTime;
        }

        public void setKadeliaProtocolPort(int kadeliaProtocolPort) {
            this.kadeliaProtocolPort = kadeliaProtocolPort;
        }

        public void setHttpPort(int httpPort) {
            this.httpPort = httpPort;
        }
    }
}
