package com.scorpionglitch.jobmanager.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Service
@Configurable
public class AvalancheJobs {

	@Autowired
	JobManagerRepository jobManagerRepository;

	// ring avalancheAPI =
	// "https://api.teamtailor.com/v1/jobs?include=department,location,locations&page[size]=30&page[number]=";
	String avalancheAPI = "https://api.teamtailor.com/v1/jobs?include=department,location,locations&location[id]=87297&page[size]=30&page[number]=";

	Logger logger = LoggerFactory.getLogger(AvalancheJobs.class);

	HttpHeaders headers;
	HttpEntity<String> entity;

	public AvalancheJobs() {
		headers = new HttpHeaders();

		headers.set("Accept", "application/vnd.api+json");
		headers.set("Authorization", "Token token=81eHATtJI0ByQcGqJbwhQvsqRYH3iIv-XSIAd0MC");
		headers.set("host", "api.teamtailor.com");
		headers.set("X-Api-Version", "20161108");

		entity = new HttpEntity<String>("body", headers);
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheMeta {
		@Getter
		@Setter
		private Object texts;

		@Getter
		@Setter
		@JsonProperty(value = "record-count")
		private int recordCount;

		@Getter
		@Setter
		@JsonProperty(value = "page-count")
		private int pageCount;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJobLinks {
		@Getter
		@Setter
		@JsonProperty(value = "careersite-job-url")
		private String careersiteJobURL;

		@Getter
		@Setter
		@JsonProperty(value = "careersite-job-apply-url")
		private String careersiteJobApplyURL;

		@Getter
		@Setter
		@JsonProperty(value = "careersite-job-apply-iframe-url")
		private String careersiteJobApplyIFrameURL;

		@Getter
		@Setter
		private String self;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJobRelationshipsLocationsLinks {
		@Getter
		@Setter
		private String self;

		@Getter
		@Setter
		private String related;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJobRelationshipsLocationsData {
		@Getter
		@Setter
		private String type;

		@Getter
		@Setter
		private String id;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJobRelationshipsLocations {
		@Getter
		@Setter
		private AvalancheJobRelationshipsLocationsLinks links;

		@Getter
		@Setter
		private AvalancheJobRelationshipsLocationsData[] data;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJobRelationships {
		@Getter
		@Setter
		private AvalancheJobRelationshipsLocations locations;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJobAttributes {
		@Getter
		@Setter
		private String title;

		@Getter
		@Setter
		private String pitch;

		@Getter
		@Setter
		@JsonProperty(value = "updated-at")
		private String updatedAt;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalancheJob {
		@Getter
		@Setter
		private long id;

		@Getter
		@Setter
		private String type;

		@Getter
		@Setter
		private AvalancheJobLinks links;

		@Getter
		@Setter
		private AvalancheJobAttributes attributes;

		@Getter
		@Setter
		private AvalancheJobRelationships relationships;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalanceIncluded {
		@Getter
		@Setter
		private long id;

		@Getter
		@Setter
		private String type;

		@Getter
		@Setter
		private Object links;

		@Getter
		@Setter
		private Object attributes;

		@Getter
		@Setter
		private Object relationships;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class AvalanchePage {
		@Getter
		@Setter
		private AvalancheJob[] data;

		@Getter
		@Setter
		private AvalanceIncluded[] included;

		@Getter
		@Setter
		private AvalancheMeta meta;

		@Getter
		@Setter
		private AvalancheJobLinks links;
	}

//	@Autowired
	private RestTemplate template = new RestTemplate();

	public String getName() {
		return "Avalanche";
	}

	public String getType() {
		return "API";
	}

	private LocalDateTime getAsLocalDateTime(String pubDate) {
		int year = Integer.parseInt(pubDate.substring(0, 4));
		int dayOfMonth = Integer.parseInt(pubDate.substring(8, 10));
		int month = Integer.parseInt(pubDate.substring(5, 7));
		int hour = Integer.parseInt(pubDate.substring(11, 13));
		int minute = Integer.parseInt(pubDate.substring(14, 16));

		return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
	}

	private void getJobs(int page) {
		try {
			ResponseEntity<AvalanchePage> avalanchePageRE = template.exchange(avalancheAPI + page, HttpMethod.GET,
					entity, AvalanchePage.class);

			if (avalanchePageRE.hasBody()) {
				AvalanchePage avalanchePage = avalanchePageRE.getBody();

				for (AvalancheJob avalancheJob : avalanchePage.data) {
					for (AvalancheJobRelationshipsLocationsData location : avalancheJob.relationships.locations.data) {
						if (location.id.compareTo("87297") == 0) {
							try {
								Job job = new Job();
								job.setSource(getName());
								job.setTitle(avalancheJob.getAttributes().getTitle());
								job.setLinkAddress(avalancheJob.links.careersiteJobURL);
								job.setPublishedDate(getAsLocalDateTime(avalancheJob.attributes.updatedAt));
								job.setDescription(avalancheJob.attributes.getPitch());

								Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());

								if (jobFromDatabase == null) {
									jobManagerRepository.save(job);
								} else {
									jobFromDatabase.setLastUpdated(null);
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
			hcee.printStackTrace();
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();

		getJobs(1);

		long end = System.currentTimeMillis();

		logger.info("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}
}
