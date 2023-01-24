package com.scorpionglitch.jobmanager.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.scorpionglitch.jobmanager.component.EmailService;
import com.scorpionglitch.jobmanager.model.Job;
import com.scorpionglitch.jobmanager.repository.JobManagerRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Service
@Configurable
public class UbisoftJobs {
	@Autowired
	EmailService emailService;

	@Autowired
	JobManagerRepository jobManagerRepository;

	Logger logger = LoggerFactory.getLogger(UbisoftJobs.class);

	RestTemplate template = new RestTemplate();

	HttpHeaders headers;
	HttpEntity<UbisoftBody> entity;
	
	private static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.append(DateTimeFormatter.ISO_LOCAL_DATE)
			.appendLiteral('T')
			.appendValue(java.time.temporal.ChronoField.HOUR_OF_DAY)
			.appendLiteral(':')
			.appendValue(java.time.temporal.ChronoField.MINUTE_OF_HOUR, 2)
			.optionalStart()
			.appendLiteral(':')
			.appendValue(java.time.temporal.ChronoField.SECOND_OF_MINUTE, 2)
			.appendFraction(java.time.temporal.ChronoField.NANO_OF_SECOND, 0, 3, true)
			.appendLiteral('Z')
			.toFormatter();

	private final static String URL_ADDRESS = "https://avcvysejs1-dsn.algolia.net/1/indexes/*/"
			+ "queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.8.4)"
			+ "%3B%20Browser%20(lite)%3B%20JS%20Helper%20(3.11.0)"
			+ "%3B%20react%20(16.12.0)%3B%20react-instantsearch%20(6.8.3)"
			+ "&x-algolia-api-key=7d1048c332e18838e52ed9d41a50ac7b" + "&x-algolia-application-id=AVCVYSEJS1";
	
	public final static String NAME = "Ubisoft";

	public UbisoftJobs() {
		headers = new HttpHeaders();
		headers.set("Accept", "*/*");
		// headers.set("Accept-Encoding", "gzip, deflate, br");
		headers.set("Accept-Language", "en-US,en;q=0.9");
		headers.set("Connection", "keep-alive");
		headers.set("Content-Length", "1127");
		headers.set("Host", "avcvysejs1-dsn.algolia.net");
		headers.set("Origin", "https://www.ubisoft.com");
		headers.set("Referer", "https://www.ubisoft.com/");
		headers.set("Sec-Fetch-Dest", "empty");
		headers.set("Sec-Fetch-Mode", "cors");
		headers.set("Sec-Fetch-Site", "cross-site");
		headers.set("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
		headers.set("content-type", "application/x-www-form-urlencoded");
		headers.set("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
		headers.set("sec-ch-ua-mobile", "?0");
		headers.set("sec-ch-ua-platform", "\"Windows\"");
		headers.setContentType(MediaType.APPLICATION_JSON);

		UbisoftBody body = new UbisoftBody(new UbisoftRequest[] { new UbisoftRequest("jobs_en-us_default",
				"facetFilters=%5B%5B%22jobFamily%3AProgramming%2C%20IT%20%26%20Technology%22%5D%2C%5B%22cities%3ANew%20York%22%5D%5D&facets=%5B%22jobFamily%22%2C%22team%22%2C%22countryCode%22%2C%22cities%22%2C%22contractType%22%2C%22workFlexibility%22%2C%22graduateProgram%22%5D&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&maxValuesPerFacet=100&page=0&query=&tagFilters="),
				new UbisoftRequest("jobs_en-us_default",
						"analytics=false&clickAnalytics=false&facetFilters=%5B%5B%22cities%3ANew%20York%22%5D%5D&facets=jobFamily&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&hitsPerPage=0&maxValuesPerFacet=100&page=0&query="),
				new UbisoftRequest("jobs_en-us_default",
						"analytics=false&clickAnalytics=false&facetFilters=%5B%5B%22jobFamily%3AProgramming%2C%20IT%20%26%20Technology%22%5D%5D&facets=cities&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&hitsPerPage=0&maxValuesPerFacet=100&page=0&query=") });

		entity = new HttpEntity<UbisoftBody>(body, headers);
	}

	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private static class UbisoftRequest {
		private String indexName;
		private String params;
	}

	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private static class UbisoftBody {
		private UbisoftRequest[] requests;
	}

	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private static class UbisoftHit {
		private String description;
		private String slug;
		private String createdAt;
		private String title;
	}

	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private static class UbisoftResult {
		private UbisoftHit[] hits;
		private Long page;
		private Long nbPages;
		private Long hitsPerPage;
		// ...
	}

	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private static class UbisoftPage {
		private UbisoftResult[] results;
	}

	@Scheduled(fixedDelay = 5L * 60L * 1000L, initialDelay = 5 * 1000L)
	@Async
	public void updateJobs() {
		long start = System.currentTimeMillis();

		ResponseEntity<UbisoftPage> ubisoftPageRE = template.exchange(URL_ADDRESS, HttpMethod.POST, entity,
				UbisoftPage.class);

		UbisoftPage ubisoftPage = ubisoftPageRE.getBody();
		
		for (UbisoftResult result : ubisoftPage.results) {
			for (UbisoftHit hit : result.hits) {
				Job job = new Job();
				job.setSource(NAME);
				job.setTitle(hit.getTitle());
				job.setDescription(hit.getDescription());
				job.setPublishedDate(LocalDateTime.parse(hit.getCreatedAt(), formatter));
				job.setLinkAddress(
						"https://www.ubisoft.com/en-us/company/careers/search/" + hit.slug);
				Job jobFromDatabase = jobManagerRepository.findByLinkAddress(job.getLinkAddress());
				if (jobFromDatabase == null) {
					jobManagerRepository.save(job);
					emailService.sendJobEmail(job);
				} else {
					jobFromDatabase.setLastUpdated(null);
					jobManagerRepository.save(jobFromDatabase);
				}
			}
		}
		
		long end = System.currentTimeMillis();
		logger.info("Updated by \"{}\": {}", Thread.currentThread().getName(), (end - start));
	}
}
