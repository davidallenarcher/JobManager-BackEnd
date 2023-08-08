package com.scorpionglitch.jobmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.service.jobs.JobManagerService;

import jakarta.validation.Valid;

@RestController
public class JobManagerController {

	Logger logger = LoggerFactory.getLogger(JobManagerController.class);

	@Autowired
	private JobManagerService jobManagerService;

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping(path = "/jobs")
	public Iterable<JobDetails> getAllJobs() {
		return jobManagerService.getAllJobs();
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping(path = "/job/{id}")
	public ResponseEntity<JobDetails> getJob(@PathVariable Long id) {
		return jobManagerService.getJobOptional(id);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@DeleteMapping(path = "/job/{id}")
	public void deleteJob(@PathVariable Long id) {
		//logger.warn("Deleting: {}", id);
		jobManagerService.deleteJob(id);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PatchMapping(path = "/job/{id}/status")
	public ResponseEntity<JobDetails> updateJobStatus(@PathVariable Long id, @Valid @RequestBody JobDetails job) {
		//logger.warn("Updating Job({}).Status:{}", id, job.getJobStatus());
		return jobManagerService.updateJobStatus(id, job);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PatchMapping(path = "/job/{id}/notes")
	public ResponseEntity<JobDetails> updateNotes(@PathVariable Long id, @Valid @RequestBody JobDetails job) {
		//logger.warn("Updating Job({}).Notes:{}", id, job.getNotes());
		return jobManagerService.updateJobNotes(id, job);
	}
}
