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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;

import lombok.Data;

@Service
@Configurable
public class DeprecatedTakeTwoJobs {
	private static final String NAME = "TakeTwo";
	private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedTakeTwoJobs.class);
	private static final RestTemplate TEMPLATE = new RestTemplate();
	private static final String OFFICES_URL_ADDRESS = "https://boards-api.greenhouse.io/v1/boards/taketwo/offices/";
	private static final String JOB_URL_ADDRESS = "https://boards-api.greenhouse.io/v1/boards/taketwo/jobs/";

	private static final String LOCATION_NEW_YORK = "48422";
	private static final String LOCATION_REMOTE = "83926";

	@Autowired
	private EmailService emailService;

	@Autowired
	private JobManagerRepository jobManagerRepository;

	private String getDesciption(long id) {
		String description = null;
		try {
			URI uri = new URI(JOB_URL_ADDRESS + id);
			TakeTwoJob takeTwoJob = TEMPLATE.getForObject(uri, TakeTwoJob.class);
			description = takeTwoJob.getContent();
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		} catch (UnknownContentTypeException ucte) {
			ucte.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		return description;
	}

	private void updateJobs(String location) {
		try {
			TakeTwoJobsResponse takeTwoJobsResponse = TEMPLATE.getForObject(OFFICES_URL_ADDRESS + location,
					TakeTwoJobsResponse.class);
			if (takeTwoJobsResponse != null && takeTwoJobsResponse.departments != null) {
				for (TakeTwoDepartment department : takeTwoJobsResponse.departments) {
					if (department != null && department.jobs != null) {
						for (TakeTwoJob takeTwoJob : department.jobs) {
							JobDetails job = new JobDetails();
							job.setSource(NAME);
							job.setTitle(takeTwoJob.getTitle());
							job.setLinkAddress(takeTwoJob.getAbsoluteUrl());
							job.setPublishedDate(LocalDateTime.parse(takeTwoJob.getUpdatedAt(),
									DateTimeFormatter.ISO_OFFSET_DATE_TIME));
							JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
							if (jobFromDatabase == null) {
								job.setDescription(getDesciption(takeTwoJob.getId()));
								jobManagerRepository.save(job);
								emailService.queueJobEmail(job);
							} else {
								jobFromDatabase.setLastSeen(LocalDateTime.now());
								jobManagerRepository.save(jobFromDatabase);
							}
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
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 0 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		updateJobs(LOCATION_NEW_YORK);
		updateJobs(LOCATION_REMOTE);
		long end = System.currentTimeMillis();
		LOGGER.debug("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}

	@Data
	private static class TakeTwoJob {
		private Long id;
		@JsonProperty(value = "absolute_url")
		private String absoluteUrl;
		@JsonProperty(value = "updated_at")
		private String updatedAt;
		private String title;
		private String content;
	}

	@Data
	private static class TakeTwoDepartment {
		private TakeTwoJob[] jobs;
	}

	@Data
	private static class TakeTwoJobsResponse {
		private TakeTwoDepartment[] departments;
	}
}
