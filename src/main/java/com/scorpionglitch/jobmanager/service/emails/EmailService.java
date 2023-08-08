package com.scorpionglitch.jobmanager.service.emails;

import com.scorpionglitch.jobmanager.model.JobDetails;

public interface EmailService {
	void queueJobEmail(JobDetails job);
	void sendQueuedEmails();
}
