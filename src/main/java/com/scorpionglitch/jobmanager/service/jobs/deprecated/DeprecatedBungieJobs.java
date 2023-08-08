package com.scorpionglitch.jobmanager.service.jobs.deprecated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;
import com.scorpionglitch.jobmanager.service.emails.EmailService;

import de.vogella.rss.model.Feed;
import de.vogella.rss.model.FeedMessage;
import de.vogella.rss.read.RSSFeedParser;

//@Service
@Deprecated
@Configurable
public class DeprecatedBungieJobs {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedBungieJobs.class);
	private static final String URL_ADDRESS = "https://careers.bungie.com/feed/index.xml";
	private static final String NAME = "Bungie";
	
	@Autowired
	private EmailService emailService;
	@Autowired
	private JobManagerRepository jobManagerRepository;

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		RSSFeedParser parser = new RSSFeedParser(URL_ADDRESS);
		Feed feed = parser.readFeed();
		List<FeedMessage> messages = feed.getMessages();
		for (int i = 0; i < messages.size(); i++) {
			FeedMessage feedMessage = messages.get(i);
			JobDetails job = new JobDetails();
			job.setSource(NAME);
			job.setTitle(feedMessage.getTitle());
			job.setLinkAddress(feedMessage.getLink());
			job.setDescription(feedMessage.getDescription());
			job.setPublishedDate(LocalDateTime.parse(feedMessage.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME));

			JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());

			if (jobFromDatabase == null) {
				jobManagerRepository.save(job);
				emailService.queueJobEmail(job);
			} else {
				jobFromDatabase.setLastSeen(LocalDateTime.now());
				jobManagerRepository.save(jobFromDatabase);
			}
		}
		long end = System.currentTimeMillis();
		LOGGER.debug("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}
}
