package com.soriole.kademlia.core.messages;

/**
 * NonKademliaMessages can be used to add new application level
 * features above the kademlia layer.
 * 
 * This is used by the ExtendedKademliaDHT class.
 */
@MessageType(type = 10)
public class NonKademliaMessage extends RawMessage{
}
