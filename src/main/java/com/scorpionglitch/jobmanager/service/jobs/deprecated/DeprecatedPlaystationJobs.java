package com.scorpionglitch.jobmanager.service.jobs.deprecated;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;

import lombok.Data;

//@Service
@Deprecated
@Configurable
public class DeprecatedPlaystationJobs {
	private static final String NAME = "Playstation";
	private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedPlaystationJobs.class);
	private static final RestTemplate TEMPLATE = new RestTemplate();
	private static final String JOBS_URL_ADDRESS = "https://boards-api.greenhouse.io/v1/boards/sonyinteractiveentertainmentglobal/jobs/";

	private static final String LOCATION_REMOTE = "United States, Remote";

	@Autowired
	private EmailService emailService;

	@Autowired
	private JobManagerRepository jobManagerRepository;

	private String getDesciption(long id) {
		String description = null;
		try {
			URI uri = new URI(JOBS_URL_ADDRESS + id);
			PlaystationJob playstationJob = TEMPLATE.getForObject(uri, PlaystationJob.class);
			description = playstationJob.getContent();
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		} catch (UnknownContentTypeException ucte) {
			ucte.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		return description;
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 0 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		try {
			PlaystationJobsResponse playstationJobsResponse = TEMPLATE.getForObject(JOBS_URL_ADDRESS,
					PlaystationJobsResponse.class);
			if (playstationJobsResponse != null && playstationJobsResponse.jobs != null) {
				for (PlaystationJob playstationJob : playstationJobsResponse.jobs) {
					if (playstationJob.location.getName().compareToIgnoreCase(LOCATION_REMOTE) == 0) {
						JobDetails job = new JobDetails();
						job.setSource(NAME);
						job.setTitle(playstationJob.getTitle());
						job.setLinkAddress(playstationJob.getAbsoluteUrl());
						job.setPublishedDate(LocalDateTime.parse(playstationJob.getUpdatedAt(),
								DateTimeFormatter.ISO_OFFSET_DATE_TIME));
						JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
						if (jobFromDatabase == null) {
							job.setDescription(getDesciption(playstationJob.getId()));
							jobManagerRepository.save(job);
							emailService.queueJobEmail(job);
						} else {
							jobFromDatabase.setLastSeen(LocalDateTime.now());
							jobManagerRepository.save(jobFromDatabase);
						}
					}
				}
			}
		} catch (RestClientException e) {
			e.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		LOGGER.debug("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}

	@Data
	private static class PlaystationJobLocation {
		private String name;
	}
	
	@Data
	private static class PlaystationJob {
		private Long id;
		@JsonProperty(value = "absolute_url")
		private String absoluteUrl;
		@JsonProperty(value = "updated_at")
		private String updatedAt;
		private String title;
		private String content;
		private PlaystationJobLocation location;
	}

	@Data
	private static class PlaystationJobsResponse {
		private PlaystationJob[] jobs;
	}
}
