package com.scorpionglitch.jobmanager.service.jobs;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.scorpionglitch.jobmanager.model.JobDetails;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

@Service
public class JobManagerService {
	Logger logger = LoggerFactory.getLogger(JobManagerService.class);

	@Autowired
	JobManagerRepository jobManagerRepository;

	public Iterable<JobDetails> getAllJobs() {
		return jobManagerRepository.findAll();
	}

	public ResponseEntity<JobDetails> getJobOptional(Long id) {
		JobDetails job = jobManagerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found for this id :: " + id));
		return ResponseEntity.ok().body(job);
	}

	public void deleteJob(Long id) {
		jobManagerRepository.deleteById(id);
	}

	public ResponseEntity<JobDetails> updateJobStatus(Long id, JobDetails job) {
		JobDetails dataBaseJob = jobManagerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found for this id :: " + id));
		dataBaseJob.setJobStatus(job.getJobStatus());
		dataBaseJob.setLastUpdated(LocalDateTime.now());
		final JobDetails updatedJob = jobManagerRepository.save(dataBaseJob);
		return ResponseEntity.ok().body(updatedJob);
	}

	public ResponseEntity<JobDetails> updateJobNotes(Long id, JobDetails job) {
		JobDetails dataBaseJob = jobManagerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found for this id :: " + id));
		dataBaseJob.setNotes(job.getNotes());
		dataBaseJob.setLastUpdated(LocalDateTime.now());
		final JobDetails updatedJob = jobManagerRepository.save(dataBaseJob);
		return ResponseEntity.ok().body(updatedJob);
	}
}
