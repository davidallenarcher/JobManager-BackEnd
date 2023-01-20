package com.scorpionglitch.jobmanager.repository;

import org.springframework.data.repository.CrudRepository;

import com.scorpionglitch.jobmanager.model.Job;

public interface JobManagerRepository extends CrudRepository<Job, Long>{
	Job findByLinkAddress(String linkAddress);
}
