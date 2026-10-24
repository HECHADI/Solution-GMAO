package com.gmao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing   // Active l'horodatage automatique
@EnableAsync         // Permet la génération PDF & Email en tâche de fond
public class GmaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmaoApplication.class, args);
	}

}
