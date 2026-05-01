package com.Gemini.AssetVault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AssetVaultApplication {
	public static void main(String[] args) {
		SpringApplication.run(AssetVaultApplication.class, args);
	}
}
