package com.scorpionglitch.jobmanager.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "JOB_TABLE")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Job {
	
	public static enum JobStatus {
		NEW,
		IGNORE,
		APPLIED,
		SHOULD_APPLY
	}
	
	@Id
	@GeneratedValue
	@Getter
	@Setter
	private Long id;

	
	@Getter
	@Setter
	@Column(columnDefinition = "varchar(32) default 'NEW'", nullable = false)
	@Enumerated(EnumType.STRING)
	private JobStatus jobStatus = JobStatus.NEW;
	
	@Getter
	@Setter
	@NotBlank(message = "source must not be empty")
	private String source;
	
	@Getter
	@Setter
	@NotBlank(message = "name must not be empty")
	private String title;
	
	@Getter
	@Setter
	@Column(nullable = false, unique = true)
	@NotBlank(message = "link must not be empty")
	private String linkAddress;

	@Getter
	@Setter
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	@JsonFormat(pattern = "dd/MM/yyyy hh:mm")
	private LocalDateTime publishedDate;
	
	@Getter
	@Setter
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	@JsonFormat(pattern = "dd/MM/yyyy hh:mm")
	@CreatedDate
	@Column(columnDefinition = "TIMESTAMP", nullable = false)
	private LocalDateTime createdDate;
	
	@Getter
	@Setter
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	@JsonFormat(pattern = "dd/MM/yyyy hh:mm")
	@LastModifiedDate
	@Column(columnDefinition = "TIMESTAMP", nullable = false)
	private LocalDateTime lastUpdated;

	@Lob
	@Column( length = 1024 )
	@Getter
	@Setter
	private String notes;
	
	@Lob
	@Column( length = 10240 )
	@Getter
	@Setter
	private String description;
}
