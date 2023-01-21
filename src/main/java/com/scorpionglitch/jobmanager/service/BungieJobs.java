package com.scorpionglitch.jobmanager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.scorpionglitch.jobmanager.component.EmailService;
import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

import de.vogella.rss.model.Feed;
import de.vogella.rss.model.FeedMessage;
import de.vogella.rss.read.RSSFeedParser;

@Service
@Configurable
public class BungieJobs {
	@Autowired
	EmailService emailService;
	
	Logger logger = LoggerFactory.getLogger(BungieJobs.class);

	@Autowired
	JobManagerRepository jobManagerRepository;

	private String urlAddress = "https://careers.bungie.com/feed/index.xml";

	public BungieJobs() {
	}

	private LocalDateTime getAsLocalDateTime(String pubDate) {
		int year = Integer.parseInt(pubDate.substring(12, 16));
		int dayOfMonth = Integer.parseInt(pubDate.substring(5, 7));
		int hour = Integer.parseInt(pubDate.substring(17, 19));
		int minute = Integer.parseInt(pubDate.substring(20, 22));
		String monthInitials = pubDate.substring(8, 11);
		int month = 0;
		switch (monthInitials) {
		case "Jan":
			month = 1;
			break;
		case "Feb":
			month = 2;
			break;
		case "Mar":
			month = 3;
			break;
		case "Apr":
			month = 4;
			break;
		case "May":
			month = 5;
			break;
		case "Jun":
			month = 6;
			break;
		case "Jul":
			month = 7;
			break;
		case "Aug":
			month = 8;
			break;
		case "Sep":
			month = 9;
			break;
		case "Oct":
			month = 10;
			break;
		case "Nov":
			month = 11;
			break;
		case "Dec":
			month = 12;
			break;
		}

		return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();
		RSSFeedParser parser = new RSSFeedParser(urlAddress);
		Feed feed = parser.readFeed();
		List<FeedMessage> messages = feed.getMessages();
		for (int i = 0; i < messages.size(); i++) {
			FeedMessage feedMessage = messages.get(i);
			Job job = new Job();
			job.setSource(getName());
			job.setTitle(feedMessage.getTitle());
			job.setLinkAddress(feedMessage.getLink());
			job.setDescription(feedMessage.getDescription());
			job.setPublishedDate(getAsLocalDateTime(feedMessage.getPubDate()));

			Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());

			if (jobFromDatabase == null) {
				jobManagerRepository.save(job);
				emailService.sendJobEmail(job);
			} else {
				jobFromDatabase.setLastUpdated(null);
				jobManagerRepository.save(jobFromDatabase);
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}

	public String getName() {
		return "Bungie";
	}

	public String getType() {
		return "RSS";
	}
}
