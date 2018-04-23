package com.soriole.kademlia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KademliaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KademliaApplication.class, args);
	}
}
