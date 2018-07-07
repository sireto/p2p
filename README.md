# Distributed network for p2p

**DISCLAIMER!** This is a work in progress implementation of Blockchain p2p messaging application.  Not recommended to be used in production yet.

# Steps to run the application
- Default configuration options are present in `src/main/resources/application.properties`
- Run command: `mvn spring-boot:run`



KademliaDHT class:
kademliaDHT provides access to the dht features. The components required to make a DHT are:
-- Contact Bucket : It stores information about available peers.
-- Kademlia Server: Responsible for sending and receiving messages to and from peers.
-- Value Store    : Stores (Key->Value) pairs in a HashTable

Setting up Kademlia DHT

        // create the identifier key
        KademliaConfig.Builder configBuilder=KademliaConfig.newBuilder();
        // over
        configBuilder.setKadeliaProtocolPort(9999);
        configBuilder.setK(3);

        Key key=new Key("ab1245")
        // create a bucket Instance
        ContactBucket bucket=new ContactBucket()

        // if the key is zero create a random key.
        Key localKey = new Key(localKeyValue);
        if(localKeyValue.equals(new Key("0"))){
            byte[] info=new byte[20];
            new Random().nextBytes(info);
            localKey=new Key(info);

        }
        if(localKeyValue.equals(bootstrapKeyValue)) {
            localPort=bootstrapPort;
        }

        configBuilder.setKadeliaProtocolPort(localPort);
        configBuilder.setK(bucketSize);

        // create kademliaExtendedDHT Instance using the autowired storageService
        kademliaDHT=new KademliaExtendedDHT(localKey,storageService,configBuilder.build());
        kademliaDHT.start();
        if (!localKeyValue.equals(bootstrapKeyValue)) {
            for(int i=0;i<4;i++) {
                if (kademliaDHT.join(new NodeInfo(new Key(bootstrapKeyValue), new InetSocketAddress(bootstrapIp, bootstrapPort)))) {
                    return;


