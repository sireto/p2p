Blockchain p2p messaging
=========================

**DISCLAIMER!** This is a work in progress implementation of Blockchain p2p messaging application.  Not recommended to be used in production yet.

## Steps to run the application

- Default configuration options are present in `src/main/resources/application.properties`

- Build command: `./gradlew bootJar`

- Start bootstrap node : `java -jar -Dserver.port=8080 -Dbootstrap=true -Dlocal.key=node1 -Dlocal.address.port=1200 ./build/libs/kademlia-standalone.jar`

- Start another node and connect to bootstrap node:
 `java -jar -Dserver.port=8081  -Dlocal.key=node2 -Dbootstrap.address.port=1200 ./build/libs/kademlia-standalone.jar`

- Start yet another node and connect to bootstrap node:
 `java -jar -Dserver.port=8082 -Dlocal.key=node3 -Dbootstrap.address.port=1200 ./build/libs/kademlia-standalone.jar`

**Available api endpoints**

| Endpoint        | Example Usage           | Output  | ---------Description---------- |
| --------------- |:-----------------:| :-------| -----------------------------------:|
| /routing_table      | localhost:8080/api/kademlia/v1/routingtable | `{"nodeInfo":[{"inetAddress":"localhost", "port":35841, "key":"node1"}]}` | Provides list of nodes connected to this node|
| /key      | localhost:8080/api/kademlia/v1/key     |   `node1` | Returns Id of this node.
| /start | localhost:8080/api/kademlia/v1/start      |    `STARTED` or `Server Already Running` | If the node is turned off it is started
| /stop | localhost:8080/api/kademlia/v1/stop      |    `STOPPED` or `Server was already not running.` | If the node is turned off it is started
| /find_nodes/{key} | localhost:8080/api/kademlia/v1/find_nodes/abc <br/><br/> localhost:8080/api/kademlia/v1/find_nodes/aaa      | `{"nodeInfo":[ {"inetAddress":"localhost","port":42985,"key":"abc"},  {"inetAddress":"localhost","port":38945,"key":"def"}, {"inetAddress":"localhost","port":1200,"key":"noddz"} ]}` <br/><br/> `{"nodeInfo":[ {"inetAddress":"localhost","port":38945,"key":"def"}, {"inetAddress":"localhost","port":42985,"key":"abc"}, {"inetAddress":"localhost","port":1200,"key":"noddz"} ]}`    | Finds the nodes closest to given id. If the node searched is not in the list, it means that the searched node doesn't exist in the network.
|/store/{key}:{value}| localhost:8080/api/kademlia/v1/store/1:Antler <br/><br/>localhost:8080/api/kademlia/v1/store/6000000:Brew?clone=2| `4`<br/><br/>`2` | Stores the given key value in the distributed network. If returned value is the no of nodes in which the value was stored. `clone` parameter can also be passed to specify how many nodes should store the value default vaue is the `K` parameter of kademlia
|/fetch/{key} | http://localhost:8081/api/kademlia/v1/fetch/1<br/><br/>http://localhost:8080/api/kademlia/v1/fetch/6000000<br/><br/>http://localhost:8080/api/kademlia/v1/fetch/6000001|`Antler`<br/><br/>`Brew`<br/><br/>`Null`| Finds the value corresponding to the key. The value need not be stored in local node, it can be fetched from the network if it exists. If it doesn't exist, Null is returned.|
|/ping/{nodeid}|http://localhost:8080/api/kademlia/v1/ping/abc<br/><br/>http://localhost:8080/api/kademlia/v1/ping/ghi<br/><br/>http://localhost:8080/api/kademlia/v1/ping/def|`Peer took 1ms to reply`<br/><br/>`Node not found in DHT Network`<br/><br/>`Peer didn't reply`|Ping a node in the network. Retuns the time taken by the node to reply.


# Using as a Library
```groovy
    repositories{
        maven {
                url 'http://playground2.sireto.com:8081/artifactory/release'
        }
    }
    dependencies{
        compile(group: 'com.soriole', name: 'kademlia', version: '2.0.2')
    }
    
```
**"KademliaDHT" class**

kademliaDHT provides access to the dht features. The components required to make a DHT are:

* Contact Bucket : It stores information about available peers.
* Kademlia Server: Responsible for sending and receiving messages to and from peers.
* Value Store    : Stores (Key->Value) pairs in a HashTable
.

**Setting up Kademlia DHT**

```java
public class Test{
    public static void main(String args[]){
        // create a configuration builder class and set the parameters as per requirement
        KademliaConfig.Builder configBuilder=KademliaConfig.newBuilder();
        configBuilder.setKadeliaProtocolPort(9999);
        configBuilder.setK(3);
        KademliaConfig config=configBuilder.build();
        
        // create identifier key for your DHT node keys are binary values and serilized using base58 encoding
        Key key=new Key("ab1245");
        
        // create a contact bucket Instance
        ContactBucket bucket=new ContactBucket(key,config);
        
        // storage for the DHT use the inmemory Store for testing
        InMemoryByteStore dhtStore = new InMemoryByteStore(config);
        
        //create a message dispacher instance. tcp and udp dispachers are available.
        MessageDispacher dispacher=new UdpServer(bucket,dhtStore,config);
        
        // create kademliaExtendedDHT Instance using the autowired storageService
        KademliaDHT dht=new KademliaDHT(bucket,disacher,dhtStore,config);
        
        // additionally if you want to connect this kademlia Dht with another Dht node
        dht.join(new InetSocketAddress("localhost",__kademlia_port_of_another_instance));
    }
}
```
