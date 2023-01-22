package com.scorpionglitch.jobmanager.service;

import java.io.IOException;

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

import com.scorpionglitch.jobmanager.component.EmailService;
import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

@Service
@Configurable
public class AtariJobs {
	@Autowired
	EmailService emailService;

	@Autowired
	JobManagerRepository jobManagerRepository;

	Logger logger = LoggerFactory.getLogger(AtariJobs.class);

	private String atariURLAddress = "https://atari.com/pages/careers";

	public String getName() {
		return "Atari";
	}

	public String getType() {
		return "HTML";
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		try {
			long start = System.currentTimeMillis();
			Document document = Jsoup.connect(atariURLAddress).get();
			Elements jobElements = document.select(".career-link__link-wrapper");
			jobElements.forEach(jobElement -> {
				Job job = new Job();
				job.setSource(getName());
				job.setTitle(jobElement.text());
				job.setLinkAddress(jobElement.attr("href"));

				Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
				if (jobFromDatabase == null) {
					jobManagerRepository.save(job);
					emailService.sendJobEmail(job);
				} else {
					jobFromDatabase.setLastUpdated(null);
					jobManagerRepository.save(jobFromDatabase);
				}
			});
			long end = System.currentTimeMillis();

			logger.info("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
