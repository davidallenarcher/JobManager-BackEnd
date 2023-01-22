package com.scorpionglitch.jobmanager.service;

import java.net.URI;
import java.net.URISyntaxException;

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

import com.scorpionglitch.jobmanager.component.EmailService;
import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Service
@Configurable
public class RockstarJobs {
	@Autowired
	EmailService emailService;

	@Autowired
	JobManagerRepository jobManagerRepository;

//	@Autowired
	private RestTemplate template = new RestTemplate();

	String rockstarJobsAPI = "https://graph.rockstargames.com/?origin=https://www.rockstargames.com&operationName=OfficeData&variables=%7B%22companySlug%22%3A%22rockstar-new-york%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%22a6691689a412cbe07a045334bdc3b4b761bc2f51e664ff84f4a9a855820689b6%22%7D%7D";
	String rockstarJobAPI = "https://graph.rockstargames.com/?origin=https://www.rockstargames.com&operationName=PositionData&variables=%7B%22positionId%22%3A{}%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%22d5ca47100cd07caf885ea9461598c512dfe017e77ff7a3e5e7150a12394ace1f%22%7D%7D";

	Logger logger = LoggerFactory.getLogger(RockstarJobs.class);

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class RockstarJob {
		@Getter
		@Setter
		private String apply_href;

		@Getter
		@Setter
		private Object company;

		@Getter
		@Setter
		private String description;

		@Getter
		@Setter
		private String department;

		@Getter
		@Setter
		private Long id;

		@Getter
		@Setter
		private String title;

		@Getter
		@Setter
		private String __typename;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class RockstarOffice {
		@Getter
		@Setter
		private String name;

		@Getter
		@Setter
		private String location;

		@Getter
		@Setter
		private String seo_url;

		@Getter
		@Setter
		private String __typename;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class RockstarJobsData {
		@Getter
		@Setter
		private RockstarOffice[] jobOffices;

		@Getter
		@Setter
		private RockstarJob[] jobsPositionList;

		@Getter
		@Setter
		private Object errors;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class RockstarPositionData {
		@Getter
		@Setter
		private RockstarJob jobsPosition;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class RockstarPositionPage {
		@Getter
		@Setter
		private RockstarPositionData data;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class RockstarJobsPage {
		@Getter
		@Setter
		private RockstarJobsData data;
	}

	public String getName() {
		return "Rockstart";
	}

	public String getType() {
		return "API";
	}

	private String getDesciption(long id) {
		String description = null;
		try {
			String urlAddress = "https://graph.rockstargames.com/?origin=https://www.rockstargames.com&"
					+ "operationName=PositionData&variables=%7B%22positionId%22%3A" + id
					+ "%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22"
					+ "%3A%22d5ca47100cd07caf885ea9461598c512dfe017e77ff7a3e5e7150a12394ace1f%22%7D%7D";
			URI uri = new URI(urlAddress);
			RockstarPositionPage rockstarPositionPage = template.getForObject(uri, RockstarPositionPage.class);
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
			URI uri = new URI(rockstarJobsAPI);

			RockstarJobsPage rockstarPage = template.getForObject(uri, RockstarJobsPage.class);

			if (rockstarPage != null && rockstarPage.data != null) {
				RockstarJob[] rockstarJobs = rockstarPage.data.jobsPositionList;
				if (rockstarJobs != null) {
					for (RockstarJob rockstarJob : rockstarJobs) {
						Job job = new Job();
						job.setSource(getName());
						job.setTitle(rockstarJob.getTitle());
						job.setLinkAddress(
								"https://www.rockstargames.com/careers/openings/position/" + rockstarJob.getId());
						Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
						if (jobFromDatabase == null) {
							job.setDescription(getDesciption(rockstarJob.getId()));
							jobManagerRepository.save(job);
							emailService.sendJobEmail(job);
						} else {
							jobFromDatabase.setLastUpdated(null);
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
		logger.info("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}

}
