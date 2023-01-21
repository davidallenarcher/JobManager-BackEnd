package com.scorpionglitch.jobmanager;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JobManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobManagerApplication.class, args);
	}

	/*
	@Bean
	CommandLineRunner run(JobManagerRepository jobRepository) {
		return (args -> {
			System.out.println("Number of jobs in repository: " + jobRepository.count());
		});
	}
	
	@Bean
	CommandLineRunner printConfiguration() {
		return (args -> {
			// stuff to run on start
		});
	}
	//*/
	
	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("David.Allen.Archer@gmail.com");
		mailSender.setPassword(System.getenv("encryption"));
		
		Properties properties = mailSender.getJavaMailProperties();
		properties.put("mail.transport.protocol", "smtp");
	    properties.put("mail.smtp.auth", "true");
	    properties.put("mail.smtp.starttls.enable", "true");
	    properties.put("mail.debug", "true");
	    
	    return mailSender;
	}
}
