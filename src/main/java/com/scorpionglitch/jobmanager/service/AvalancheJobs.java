package com.scorpionglitch.jobmanager.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scorpionglitch.jobmanager.component.EmailService;
import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

import lombok.Data;

@Service
@Configurable
public class AvalancheJobs {
	private static final String API_ADDRESS = "https://api.teamtailor.com/v1/jobs?include=department,location,locations&location[id]=87297&page[size]=30&page[number]=";
	private static final String NAME = "Avalanche";
	private static final Logger logger = LoggerFactory.getLogger(AvalancheJobs.class);
	private static final HttpEntity<String> entity; 
	static {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/vnd.api+json");
		headers.set("Authorization", "Token token=81eHATtJI0ByQcGqJbwhQvsqRYH3iIv-XSIAd0MC");
		headers.set("host", "api.teamtailor.com");
		headers.set("X-Api-Version", "20161108");
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
			ResponseEntity<AvalanchePage> avalanchePageRE = template.exchange(API_ADDRESS + page, HttpMethod.GET,
					entity, AvalanchePage.class);

			if (avalanchePageRE.hasBody()) {
				AvalanchePage avalanchePage = avalanchePageRE.getBody();

				for (AvalancheJob avalancheJob : avalanchePage.data) {
					for (AvalancheJobRelationshipsLocationsData location : avalancheJob.relationships.locations.data) {
						if (location.id.compareTo("87297") == 0) {
							try {
								Job job = new Job();
								job.setSource(NAME);
								job.setTitle(avalancheJob.getAttributes().getTitle());
								job.setLinkAddress(avalancheJob.links.careersiteJobURL);
								job.setPublishedDate(LocalDateTime.parse(avalancheJob.getAttributes().getUpdatedAt(),
										DateTimeFormatter.ISO_OFFSET_DATE_TIME));
								job.setDescription(avalancheJob.attributes.getPitch());
								Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
								if (jobFromDatabase == null) {
									jobManagerRepository.save(job);
									emailService.sendJobEmail(job);
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

				if (avalanchePage.meta.pageCount > page) {
					getJobs(page + 1);
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
	
	@Data
	private static class AvalancheMeta {
		private Object texts;
		@JsonProperty(value = "page-count")
		private int pageCount;
	}

	@Data
	private static class AvalancheJobLinks {
		@JsonProperty(value = "careersite-job-url")
		private String careersiteJobURL;
	}

	@Data
	private static class AvalancheJobRelationshipsLocationsData {
		private String id;
	}

	@Data
	private static class AvalancheJobRelationshipsLocations {
		private AvalancheJobRelationshipsLocationsData[] data;
	}

	@Data
	private static class AvalancheJobRelationships {
		private AvalancheJobRelationshipsLocations locations;
	}

	@Data
	private static class AvalancheJobAttributes {
		private String title;
		private String pitch;
		@JsonProperty(value = "updated-at")
		private String updatedAt;
	}

	@Data
	private static class AvalancheJob {
		private AvalancheJobLinks links;
		private AvalancheJobAttributes attributes;
		private AvalancheJobRelationships relationships;
	}

	@Data
	private static class AvalanchePage {
		private AvalancheJob[] data;
		private AvalancheMeta meta;
	}
}
