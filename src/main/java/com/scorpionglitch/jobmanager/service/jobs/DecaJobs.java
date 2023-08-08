package com.scorpionglitch.jobmanager.service.jobs;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;

import lombok.Data;

@Service
@Configurable
public class DecaJobs {
	private final static DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T')
			.appendValue(java.time.temporal.ChronoField.HOUR_OF_DAY).appendLiteral(':')
			.appendValue(java.time.temporal.ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
			.appendValue(java.time.temporal.ChronoField.SECOND_OF_MINUTE, 2)
			.appendFraction(java.time.temporal.ChronoField.NANO_OF_SECOND, 0, 3, true).appendLiteral('Z').toFormatter();
	private static final Logger LOGGER = LoggerFactory.getLogger(DecaJobs.class);
	private static final RestTemplate TEMPLATE = new RestTemplate();
	private static final String NAME = "Deca";
	private static final String DECA_JOBS_API = "https://deca-games.breezy.hr/json";
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private JobManagerRepository jobManagerRepository;
	
	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		try {
			URI uri = new URI(DECA_JOBS_API);
			
			DecaJob[] decaJobs = TEMPLATE.getForObject(uri, DecaJob[].class);
			
			if (decaJobs != null) {
				for (DecaJob decaJob : decaJobs) {
					JobDetails job = new JobDetails();
					job.setSource(NAME);
					job.setTitle(decaJob.getName());
					job.setLinkAddress(decaJob.getUrl());
					job.setPublishedDate(LocalDateTime.parse(decaJob.publishedDate, FORMATTER));
					JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
					if (jobFromDatabase == null) {
						// TODO get description form job.getLinkAddress()
						jobManagerRepository.save(job);
						emailService.queueJobEmail(job);
					} else {
						jobFromDatabase.setLastSeen(LocalDateTime.now());
						jobManagerRepository.save(jobFromDatabase);
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
	private static class DecaJob {
		private String url;
		private String name;
		@JsonProperty(value = "published_date")
		private String publishedDate;
	}
}
