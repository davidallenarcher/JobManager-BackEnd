package com.scorpionglitch.jobmanager.component;

import com.scorpionglitch.jobmanager.model.Job;

public interface EmailService {
	String sendSimpleEmail(EmailDetails details);
	String sendMailWithAttachment(EmailDetails details);
	String sendJobEmail(Job job);
}
