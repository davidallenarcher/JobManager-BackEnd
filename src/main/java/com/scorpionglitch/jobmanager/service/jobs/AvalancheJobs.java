package com.scorpionglitch.jobmanager.service.jobs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;

import lombok.Data;

@Service
@Configurable
public class AvalancheJobs {
	private static final String API_ADDRESS = "https://avalanchestudios.com/v0/postings/avalanchestudios";
	private static final String NAME = "Avalanche";
	private static final Logger logger = LoggerFactory.getLogger(AvalancheJobs.class);
	private static final HttpEntity<String> entity;
	static {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		entity = new HttpEntity<String>("body", headers);
	}
	private static RestTemplate template = new RestTemplate();

	@Autowired
	private EmailService emailService;
	@Autowired
	private JobManagerRepository jobManagerRepository;

	private void getJobs(int page) {
		try {
			ResponseEntity<AvalancheJob[]> avalanchePageRE = template.exchange(API_ADDRESS, HttpMethod.GET,	entity, AvalancheJob[].class);

			if (avalanchePageRE.hasBody()) {
				for (AvalancheJob avalancheJob : avalanchePageRE.getBody()) {
					if (avalancheJob.categories.location.compareTo("New York") == 0) {
						try {
							JobDetails job = new JobDetails();
							job.setSource(NAME);
							job.setTitle(avalancheJob.getText());
							job.setLinkAddress(avalancheJob.getHostedUrl());
							job.setPublishedDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(avalancheJob.createdAt), TimeZone
							        .getDefault().toZoneId()));
							job.setDescription(avalancheJob.additional);
							JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
							if (jobFromDatabase == null) {
								jobManagerRepository.save(job);
								emailService.queueJobEmail(job);
							} else {
								jobFromDatabase.setLastSeen(LocalDateTime.now());
								jobManagerRepository.save(jobFromDatabase);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}				
				}
			}

		} catch (HttpClientErrorException hcee) {
			logger.error(hcee.toString());
			hcee.printStackTrace();
		} catch (RestClientException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 0 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		getJobs(1);
		long end = System.currentTimeMillis();
		logger.debug("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}
	
	@Data static class AvalancheSalaryRange {
		private int min;
		private int max;
		private String currancy;
		private String interval; 
	}

	@Data
	private static class AvalancheList {
		private String text;
		private String content;
	}
	
	@Data
	private static class AvalancheCategories {
		private String commitment;
		private String department;
		private String location;
		private String team;
	}
	
	@Data
	private static class AvalancheJob {
		private String additional;
		private String additionalPlain;
		private AvalancheCategories categories;
		private long createdAt;
		private String descriptionPlain;
		private String description;
		private String id;
		private AvalancheList[] lists;
		private AvalancheSalaryRange salaryRange;
		private String salaryDescription;
		private String salaryDescriptionPlain;
		private String text;
		private String country;
		private String workplaceType;
		private String hostedUrl;
		private String applyUrl;
	}
}
