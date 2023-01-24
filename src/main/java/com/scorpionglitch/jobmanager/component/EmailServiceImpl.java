package com.scorpionglitch.jobmanager.component;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.scorpionglitch.jobmanager.model.Job;

@Component
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	
	private ArrayList<Job> jobs;
	
	@Override
	public String sendSimpleEmail(EmailDetails details) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom("David.Allen.Archer@gmail.com");
			mailMessage.setTo(details.getRecipient());
			mailMessage.setText(details.getMessageBody());
			mailMessage.setSubject(details.getSubject());
			
			javaMailSender.send(mailMessage);
			return "Mail Sent Successfully..";
		} catch (Exception e) {
			return "Error While Sending Mail";
		}
	}

	@Override
	public String sendMailWithAttachment(EmailDetails details) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sendJobEmail(Job job) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom("David.Allen.Archer@gmail.com");
			mailMessage.setTo("David.Allen.Archer@gmail.com");
			mailMessage.setSubject("[JobScraper] " + job.getSource() + " - " + job.getTitle());
			mailMessage.setText(
					job.getSource() + "\r\n" +
					job.getTitle() + "\r\n" +
					job.getLinkAddress() + "\r\n" +
					job.getDescription());
			javaMailSender.send(mailMessage);
			
			mailMessage = new SimpleMailMessage();
			mailMessage.setFrom("David.Allen.Archer@gmail.com");
			mailMessage.setTo("9152514415@vtext.com");
			mailMessage.setText("[JobScraper]\r\n" 
					+ job.getSource() + "\r\n" 
					+ job.getTitle() + "\r\n" 
					+ job.getLinkAddress());
			javaMailSender.send(mailMessage);
			
			return "Mail Sent Successfully..";
		} catch (Exception e) {
			return "Error While Sending Mail";
		}
	}

}
