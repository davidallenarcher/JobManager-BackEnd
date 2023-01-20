package com.scorpionglitch.jobmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.service.JobManagerService;

import jakarta.validation.Valid;

@RestController
public class JobManagerController {

	Logger logger = LoggerFactory.getLogger(JobManagerController.class);

	@Autowired
	private JobManagerService jobManagerService;

	@GetMapping(path = "/")
	public String getPlaceHolder() {
		return "Under Construction";
	}

	@GetMapping(path = "/jobs")
	public Iterable<Job> getAllJobs() {
		return jobManagerService.getAllJobs();
	}

	@GetMapping(path = "/job/{id}")
	public ResponseEntity<Job> getJob(@PathVariable Long id) {
		return jobManagerService.getJobOptional(id);
	}

	@GetMapping(path = "/test")
	public Iterable<Job> test() {
		return jobManagerService.getAllJobs();
	}

	@DeleteMapping(path = "/job/{id}")
	public void deleteJob(@PathVariable Long id) {
		logger.warn("Deleting: {}", id);
		jobManagerService.deleteJob(id);
	}

	@PatchMapping(path = "/job/{id}/status")
	public ResponseEntity<Job> updateJobStatus(@PathVariable Long id, @Valid @RequestBody Job job) {
		logger.warn("Updating Job({}).Status:{}", id, job.getJobStatus());
		return jobManagerService.updateJobStatus(id, job);
	}

	@PatchMapping(path = "/job/{id}/notes")
	public ResponseEntity<Job> updateNotes(@PathVariable Long id, @Valid @RequestBody Job job) {
		logger.warn("Updating Job({}).Notes:{}", id, job.getNotes());
		return jobManagerService.updateJobNotes(id, job);
	}
}
