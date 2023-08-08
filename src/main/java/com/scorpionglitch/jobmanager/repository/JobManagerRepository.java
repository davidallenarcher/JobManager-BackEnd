package com.scorpionglitch.jobmanager.repository;

import org.springframework.data.repository.CrudRepository;

import com.scorpionglitch.jobmanager.model.JobDetails;

public interface JobManagerRepository extends CrudRepository<JobDetails, Long>{
	JobDetails findByLinkAddress(String linkAddress);
}
