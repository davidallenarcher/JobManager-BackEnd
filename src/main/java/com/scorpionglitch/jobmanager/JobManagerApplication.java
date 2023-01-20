package com.scorpionglitch.jobmanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

@EnableScheduling
@SpringBootApplication
public class JobManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobManagerApplication.class, args);
	}

	//*
	@Bean
	CommandLineRunner run(JobManagerRepository jobRepository) {
		return (args -> {
			System.out.println("Number of jobs in repository: " + jobRepository.count());
		});
	}
	//*/
	
	@Bean
	CommandLineRunner printConfiguration() {
		return (args -> {
			// stuff to run on start
		});
	}

}
