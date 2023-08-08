package com.scorpionglitch.jobmanager.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailDetails {
	@Getter
	@Setter
	private String recipient;
	
	@Getter
	@Setter
	private String messageBody;
	
	@Getter
	@Setter
	private String subject;
	
	@Getter
	@Setter
	private String attachment;
}
