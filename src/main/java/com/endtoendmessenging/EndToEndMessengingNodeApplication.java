package com.endtoendmessenging;

import com.soriole.kademlia.KademliaApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(KademliaApplication.class)
public class EndToEndMessengingNodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EndToEndMessengingNodeApplication.class, args);
    }
}
