package com.endtoendmessenging.controller;

import com.soriole.kademlia.service.KademliaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/v1")
public class ClientApiController {

    @Autowired
    KademliaService kademliaService;

    @RequestMapping("subscribe")
    public void subscribe(@RequestParam(value="id") String id){

    }
    @RequestMapping("unsubscribe")
    public void unsubscribe(@RequestParam(value="id") String id){


    }
    @GetMapping("get_subscribed")
    public void subscribed(@RequestParam(value="id") String id){

    }
    @GetMapping("get_peer_nodes")
    public void getPeerNodes(){
    }

    @PostMapping("send_message")
    public void sendMessage(@RequestBody String clientId,@RequestBody String receiverId,@RequestBody String message){

    }
}
