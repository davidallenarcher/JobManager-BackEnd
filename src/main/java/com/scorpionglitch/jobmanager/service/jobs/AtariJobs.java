package com.scorpionglitch.jobmanager.service.jobs;

import java.io.IOException;
import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

@Service
@Configurable
public class AtariJobs {
	private static final Logger LOGGER = LoggerFactory.getLogger(AtariJobs.class);
	private static final String ATARI_URL_ADDRESS = "https://atari.com/pages/careers";
	private static final String NAME = "Atari";
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private JobManagerRepository jobManagerRepository;

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		try {
			long start = System.currentTimeMillis();
			Document document = Jsoup.connect(ATARI_URL_ADDRESS).get();
			Elements jobElements = document.select(".career-link__link-wrapper");
			jobElements.forEach(jobElement -> {
				JobDetails job = new JobDetails();
				job.setSource(NAME);
				job.setTitle(jobElement.text());
				job.setLinkAddress(jobElement.attr("href"));

				JobDetails jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
				if (jobFromDatabase == null) {
					jobManagerRepository.save(job);
					emailService.queueJobEmail(job);
				} else {
					jobFromDatabase.setLastSeen(LocalDateTime.now());
					jobManagerRepository.save(jobFromDatabase);
				}
			});
			long end = System.currentTimeMillis();

			LOGGER.debug("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));

		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e.toString());
		}
	}
}
