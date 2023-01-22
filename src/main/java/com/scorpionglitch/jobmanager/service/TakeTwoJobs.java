package com.scorpionglitch.jobmanager.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
public class TakeTwoJobs {
	@Autowired
	EmailService emailService;
	
	@Autowired
	JobManagerRepository jobManagerRepository;

	private Logger logger = LoggerFactory.getLogger(TakeTwoJobs.class);

	private RestTemplate template = new RestTemplate();

	private static final String urlAddress = "https://boards-api.greenhouse.io/v1/boards/taketwo/offices/";
	private static final String locationNewYork = "48422";
	private static final String locationRemote = "83926";

	private static final String name = "TakeTwo";

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class TakeTwoJob {
		@Getter
		@Setter
		String absolute_url;

		@Getter
		@Setter
		String updated_at;

		@Getter
		@Setter
		String title;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class TakeTwoDepartment {
		@Getter
		@Setter
		Long id;

		@Getter
		@Setter
		String name;

		@Getter
		@Setter
		Long parent_id;

		@Getter
		@Setter
		Long[] child_ids;

		@Getter
		@Setter
		TakeTwoJob[] jobs;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	private static class TakeTwoJobsResponse {
		@Getter
		@Setter
		Long id;

		@Getter
		@Setter
		String name;

		@Getter
		@Setter
		String location;

		@Getter
		@Setter
		Long parent_id;

		@Getter
		@Setter
		Long[] child_ids;

		@Getter
		@Setter
		TakeTwoDepartment[] departments;
	}

	private LocalDateTime getAsLocalDateTime(String pubDate) {
		int year = Integer.parseInt(pubDate.substring(0, 4));
		int dayOfMonth = Integer.parseInt(pubDate.substring(8, 10));
		int month = Integer.parseInt(pubDate.substring(5, 7));
		int hour = Integer.parseInt(pubDate.substring(11, 13));
		int minute = Integer.parseInt(pubDate.substring(14, 16));

		return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
	}

	private void updateJobs(String location) {
		long start = System.currentTimeMillis();
		try {
			TakeTwoJobsResponse takeTwoJobsResponse = template.getForObject(urlAddress + location, TakeTwoJobsResponse.class);
			if (takeTwoJobsResponse != null && takeTwoJobsResponse.departments != null) {
				for (TakeTwoDepartment department : takeTwoJobsResponse.departments) {
					if (department != null && department.jobs != null) {
						for (TakeTwoJob takeTwoJob : department.jobs) {
							Job job = new Job();
							job.setSource(name);
							job.setTitle(takeTwoJob.getTitle());
							job.setLinkAddress(takeTwoJob.getAbsolute_url());
							job.setPublishedDate(getAsLocalDateTime(takeTwoJob.updated_at));

							Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
							if (jobFromDatabase == null) {
								jobManagerRepository.save(job);
								emailService.sendJobEmail(job);
							} else {
								jobFromDatabase.setLastUpdated(null);
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
		long end = System.currentTimeMillis();
		logger.info("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		updateJobs(locationNewYork);
		updateJobs(locationRemote);
	}

}
