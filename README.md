# Blockchain p2p messaging

**DISCLAIMER!** This is a work in progress implementation of Blockchain p2p messaging application.  Not recommended to be used in production yet.

# Steps to run the application
- Default configuration options are present in `src/main/resources/application.properties`

- Build command: `./gradlew build -x test`

- Start bootstrap node : `java -jar -Dserver.port=8080 -Dbootstrap=true -Dlocal.address.port=1200 ./build/libs/p2p-kademlia-2.0.0.jar`

- Start another node and connect to bootstrap node:
 `java -jar -Dserver.port=8081  -Dbootstrap.address.port=1200 ./build/libs/p2p-kademlia-2.0.0.jar`

- Start yet another node and connect to bootstrap node:
 `java -jar -Dserver.port=8082  -Dbootstrap.address.port=1200 ./build/libs/p2p-kademlia-2.0.0.jar`

# "KademliaDHT" class
kademliaDHT provides access to the dht features. The components required to make a DHT are:

* Contact Bucket : It stores information about available peers.
* Kademlia Server: Responsible for sending and receiving messages to and from peers.
* Value Store    : Stores (Key->Value) pairs in a HashTable
.

###Setting up Kademlia DHT

    // create a configuration builder class and set the parameters as per requirement
    KademliaConfig.Builder configBuilder=KademliaConfig.newBuilder();
    configBuilder.setKadeliaProtocolPort(9999);
    configBuilder.setK(3);
    KademliaConfig config=configBuilder.build();

    // create identifier key for your DHT node keys are binary values and serilized using base58 encoding
    Key key=new Key("ab1245")
    
    // create a contact bucket Instance
    ContactBucket bucket=new ContactBucket(key,config);

    // storage for the DHT use the inmemory Store for testing
    InMemoryByteStore dhtStore = new InMemoryByteStore(config);

    //create a message dispacher instance. tcp and udp dispachers are available.
    MessageDispacher dispacher=new new com.soriole.kademlia.core.network.server.udp.KademliaServer(bucket,dhtStore,config)
    
    // create kademliaExtendedDHT Instance using the autowired storageService
    KademliaDHT dht=new KademliaDHT(bucket,disacher,dhtStore,config)
    
    // additionally if you want to connect this kademlia Dht with another Dht node
    dht.join(new InetSocketAddress("localhost",__kademlia_port_of_another_instance));


