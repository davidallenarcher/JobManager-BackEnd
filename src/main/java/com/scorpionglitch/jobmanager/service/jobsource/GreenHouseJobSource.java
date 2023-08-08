package com.scorpionglitch.jobmanager.service.jobsource;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scorpionglitch.jobmanager.model.JobDetails;

import lombok.Data;

public class GreenHouseJobSource {
	private static final Logger LOGGER = LoggerFactory.getLogger(GreenHouseJobSource.class);
	private static final RestTemplate TEMPLATE = new RestTemplate();
	private static final String JOBS_URL_ADDRESS = "https://boards-api.greenhouse.io/v1/boards/%s/%s/";
	
	private final Map<String, JobDetails> cachedJobs = new HashMap<String, JobDetails>();
	
	private final String companyGreenHouseID;
	private final String companyName;
	private final List<String> locations = new LinkedList<String>();
	
	public GreenHouseJobSource(String companyName, String companyGreenHouseID, String[] locations) {
		this.companyName = companyName;
		this.companyGreenHouseID = companyGreenHouseID;
		this.locations.addAll(Arrays.asList(locations));
	}
	
	public String getDesciption(long jobID) {
		String description = null;
		try {
			URI uri = new URI(String.format(JOBS_URL_ADDRESS, companyGreenHouseID, "jobs") + jobID);
			GreenHouseJob greenHouseJob = TEMPLATE.getForObject(uri, GreenHouseJob.class);
			//description = greenHouseJob.getContent();
			
			description = StringEscapeUtils.unescapeHtml4(greenHouseJob.getContent());
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		} catch (UnknownContentTypeException ucte) {
			ucte.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		return description;
	}
	
	public List<JobDetails> getJobs() {
		List<JobDetails> jobDetailsList = new LinkedList<JobDetails>(); 
		try {
			URI uri = new URI(String.format(JOBS_URL_ADDRESS, companyGreenHouseID, "jobs"));
			GreenHouseJobsResponse greenHouseJobsResponse = TEMPLATE.getForObject(uri,
					GreenHouseJobsResponse.class);
			if (greenHouseJobsResponse != null && greenHouseJobsResponse.jobs != null) {
				Arrays.asList(greenHouseJobsResponse.jobs).stream().forEach(job -> {
					if (locations.contains(job.getLocation().name)) {
						JobDetails jobDetails = cachedJobs.get(job.getAbsoluteUrl());
						if (jobDetails == null) {
							jobDetails = new JobDetails();
							jobDetails.setSource(companyName);
							jobDetails.setTitle(job.getTitle());
							jobDetails.setLinkAddress(job.getAbsoluteUrl());
							jobDetails.setPublishedDate(LocalDateTime.parse(job.getUpdatedAt(),
								DateTimeFormatter.ISO_OFFSET_DATE_TIME));
							cachedJobs.put(job.getAbsoluteUrl(), jobDetails);
						}
						jobDetailsList.add(jobDetails);
					}
				});
			}
		} catch (RestClientException e) {
			e.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jobDetailsList;
	}
	
	@Data
	private static class GreenHouseJobLocation {
		private String name;
	}
	
	@Data
	private static class GreenHouseJob {
		private Long id;
		@JsonProperty(value = "absolute_url")
		private String absoluteUrl;
		@JsonProperty(value = "updated_at")
		private String updatedAt;
		private String title;
		private String content;
		private GreenHouseJobLocation location;
	}

	@Data
	private static class GreenHouseJobsResponse {
		private GreenHouseJob[] jobs;
	}
}
