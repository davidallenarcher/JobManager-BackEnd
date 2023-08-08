package com.scorpionglitch.jobmanager.service.emails;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.scorpionglitch.jobmanager.model.JobDetails;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailServiceImpl implements EmailService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

	private List<JobDetails> queuedJobs = new LinkedList<JobDetails>();
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Override
	public void queueJobEmail(JobDetails job) {
		queuedJobs.add(job);
	}

	
	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 1L * 60L * 1000L)
	@Async
	public void sendQueuedEmails() {
		int newJobCount = queuedJobs.size();
		if (newJobCount > 0) {
			JobDetails[] jobs = new JobDetails[newJobCount];
			queuedJobs.toArray(jobs);
			
			StringBuilder jobMessage = new StringBuilder();
			
			for (JobDetails job : jobs) {
				jobMessage
					.append("<A href=\"")
					.append(job.getLinkAddress())
					.append("\" style=\"font-size:24; font-weight: bold;\">")
					.append(job.getTitle())
					.append("</A><BR/><SPAN style=\"font-weight:bold;\">")
					.append(job.getSource())
					.append("</SPAN><BR/><DIV style=\"border: 1px solid black\">")
					.append(job.getDescription())
					.append("</DIV><BR/>");
				
				queuedJobs.remove(job);
			}
			
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
			try {
				helper.setFrom("David.Allen.Archer@gmail.com");
				helper.setTo("David.Allen.Archer@gmail.com");
				helper.setSubject("[JobScraper] " + newJobCount + " New Jobs!");
				helper.setText(jobMessage.toString(), true);
				javaMailSender.send(mimeMessage);
				LOGGER.info("email sent: \"" + newJobCount + " New Jobs!\"");
			} catch (MessagingException e) {
				LOGGER.error("email not sent: ");
				LOGGER.error(e.toString());
			}
			
			sendTextNotification(newJobCount);
		}
	}
	
	private void sendTextNotification(int newJobCount) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("David.Allen.Archer@gmail.com");
		mailMessage.setTo("9152514415@vtext.com");
		mailMessage.setText(newJobCount + " New Jobs!");
		javaMailSender.send(mailMessage);
		LOGGER.info("text message sent: \"" + newJobCount + " New Jobs!\"");
	}
}
