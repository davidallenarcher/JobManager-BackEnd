package com.scorpionglitch.jobmanager.component;

import com.scorpionglitch.jobmanager.model.Job;

public interface EmailService {
	String sendJobEmail(Job job);
}
