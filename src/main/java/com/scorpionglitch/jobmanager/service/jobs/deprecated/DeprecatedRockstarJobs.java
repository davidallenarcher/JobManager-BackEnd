package com.scorpionglitch.jobmanager.service.jobs.deprecated;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;

import lombok.Data;

//@Service
@Deprecated
@Configurable
public class DeprecatedRockstarJobs {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedRockstarJobs.class);
	private static final RestTemplate TEMPLATE = new RestTemplate();
	private static final String NAME = "Rockstar";
	private static final String ROCKSTAR_JOBS_API = "https://graph.rockstargames.com/?origin=https://www.rockstargames.com&operationName=OfficeData&variables=%7B%22companySlug%22%3A%22rockstar-new-york%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%22a6691689a412cbe07a045334bdc3b4b761bc2f51e664ff84f4a9a855820689b6%22%7D%7D";
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private JobManagerRepository jobManagerRepository;
	
	private String getDesciption(long id) {
		String description = null;
		try {
			String urlAddress = "https://graph.rockstargames.com/?origin=https://www.rockstargames.com&"
					+ "operationName=PositionData&variables=%7B%22positionId%22%3A" + id
					+ "%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22"
					+ "%3A%22d5ca47100cd07caf885ea9461598c512dfe017e77ff7a3e5e7150a12394ace1f%22%7D%7D";
			URI uri = new URI(urlAddress);
			RockstarPositionPage rockstarPositionPage = TEMPLATE.getForObject(uri, RockstarPositionPage.class);
			RockstarPositionData rockstarPositionData = rockstarPositionPage.getData();
			RockstarJob rockstarJob = rockstarPositionData.getJobsPosition();
			description = rockstarJob.getDescription();
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		} catch (UnknownContentTypeException ucte) {
			ucte.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		return description;
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		try {
			URI uri = new URI(ROCKSTAR_JOBS_API);

			RockstarJobsPage rockstarPage = TEMPLATE.getForObject(uri, RockstarJobsPage.class);

			if (rockstarPage != null && rockstarPage.data != null) {
				RockstarJob[] rockstarJobs = rockstarPage.data.jobsPositionList;
				if (rockstarJobs != null) {
					for (RockstarJob rockstarJob : rockstarJobs) {
						JobDetails job = new JobDetails();
						job.setSource(NAME);
						job.setTitle(rockstarJob.getTitle());
						job.setLinkAddress(
								"https://www.rockstargames.com/careers/openings/position/" + rockstarJob.getId());
						JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
						if (jobFromDatabase == null) {
							job.setDescription(getDesciption(rockstarJob.getId()));
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
		} catch (URISyntaxException e) {
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
	private static class RockstarJob {
		private String description;
		private Long id;
		private String title;
	}

	@Data
	private static class RockstarJobsData {
		private RockstarJob[] jobsPositionList;
	}

	@Data
	private static class RockstarPositionData {
		private RockstarJob jobsPosition;
	}

	@Data
	private static class RockstarPositionPage {
		private RockstarPositionData data;
	}

	@Data
	private static class RockstarJobsPage {
		private RockstarJobsData data;
	}
}
