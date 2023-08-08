package com.scorpionglitch.jobmanager.service.jobs;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;
import com.scorpionglitch.jobmanager.service.jobsource.GreenHouseJobSource;

@Service
@Configurable
public class BungieJobs {
	private static final String NAME = "Bungie";

	private static final String GREENHOUSE_BOARD_ID = "bungie";
	private static final Logger LOGGER = LoggerFactory.getLogger(BungieJobs.class);
	private static final String[] LOCATIONS = {"Remote - Anywhere in the U.S.", "Hybrid / Bungie-Approved Remote Locations"};
	
	private static final GreenHouseJobSource jobSource = 
			new GreenHouseJobSource(NAME, GREENHOUSE_BOARD_ID, LOCATIONS);
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private JobManagerRepository jobManagerRepository;

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 0 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		
		jobSource.getJobs().stream().forEach(job -> {
			JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
			if (jobFromDatabase == null) {
				String[] segments = job.getLinkAddress().split("/");
				String idStr = segments[segments.length-1];
				long jobID = Long.parseLong(idStr);
				job.setDescription(jobSource.getDesciption(jobID));
				jobManagerRepository.save(job);
				emailService.queueJobEmail(job);
			} else {
				jobFromDatabase.setLastSeen(LocalDateTime.now());
				jobManagerRepository.save(jobFromDatabase);
			}
		});
		
		long end = System.currentTimeMillis();
		LOGGER.debug("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}
}
