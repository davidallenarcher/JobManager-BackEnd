package com.scorpionglitch.jobmanager.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "JOB_TABLE")
@Data
public class Job {
	
	public static enum JobStatus {
		NEW,
		IGNORE,
		APPLIED,
		SHOULD_APPLY,
		SEEN
	}
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(columnDefinition = "varchar(32) default 'NEW'", nullable = false)
	@Enumerated(EnumType.STRING)
	private JobStatus jobStatus = JobStatus.NEW;
	
	@NotBlank(message = "source must not be empty")
	private String source;
	
	@NotBlank(message = "name must not be empty")
	private String title;
	
	@Column(nullable = false, unique = true)
	@NotBlank(message = "link must not be empty")
	private String linkAddress;

	@Column(columnDefinition = "TIMESTAMP", nullable = true)
	private LocalDateTime publishedDate;
	
	@CreatedDate
	@Column(columnDefinition = "TIMESTAMP", nullable = false)
	private LocalDateTime createdDate;
	
	@Column(columnDefinition = "TIMESTAMP", nullable = false)
	private LocalDateTime lastSeen = LocalDateTime.now();
	
	@Column(columnDefinition = "TIMESTAMP", nullable = false)
	private LocalDateTime lastUpdated = LocalDateTime.now();

	@Lob
	@Column( length = 1024 )
	private String notes;
	
	@Lob
	@Column( length = 10240 )
	private String description;
}
