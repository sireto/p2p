package com.soriole.kademlia.core.store;

public     class SessionInfo{
    public NodeInfo receiver;
    public long sessionId;
    public SessionInfo(){}
    public SessionInfo(NodeInfo r,long s){receiver=r;sessionId=s;}
}