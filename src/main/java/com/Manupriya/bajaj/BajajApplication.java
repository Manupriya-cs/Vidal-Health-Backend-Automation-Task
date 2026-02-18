package com.Manupriya.bajaj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.Manupriya.bajaj.service.WebhookService;

@SpringBootApplication
public class BajajApplication implements CommandLineRunner {
	@Autowired
	private WebhookService webhookService;

	public static void main(String[] args) {
		SpringApplication.run(BajajApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		webhookService.startProcess();
	}

}
