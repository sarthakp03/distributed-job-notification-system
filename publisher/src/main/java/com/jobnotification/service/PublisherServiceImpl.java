package com.jobnotification.service;

import com.jobnotification.model.PublishedJobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PublisherServiceImpl implements PublisherService {

    private static final int MAX_RETRIES = 5;
    @Autowired
    RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> addPublisher(String companyName) {
        for (int i = 1; i <= MAX_RETRIES; i++) {
            if (i > 1) {
                try {
                    System.out.println("Trying to connect to broker to add publisher");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.err.println("Exception occurred: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            try {
                //String url = "http://ec2-54-196-152-211.compute-1.amazonaws.com:8081/add/publisher";
                String url = "http://localhost:8081/add/publisher";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> req = new HttpEntity<>(companyName, headers);

                ResponseEntity<HttpStatus> responseEntity = restTemplate.exchange(url,
                        HttpMethod.POST, req, HttpStatus.class);

                HttpStatus status = responseEntity.getBody();
                int code = status.value();
                if (code == HttpStatus.BAD_REQUEST.value()) {
                    System.err.println("Error while adding new publisher as provided request is bad");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("BAD REQUEST");
                } else if (code == HttpStatus.OK.value()) {
                    System.out.println("Added new publisher to broker");
                    return ResponseEntity.status(HttpStatus.OK).body("Publisher Added");
                }

            } catch (Exception ex) {
                System.err.println("Exception occurred: " + ex.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Broker is down");
    }

    @Override
    public ResponseEntity<String> addJobs(PublishedJobs publishedJobs) {
        for (int i = 1; i <= MAX_RETRIES; i++) {
            if (i > 1) {
                try {
                    System.out.println("Trying to publish job to broker");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.err.println("Exception occurred: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            try {
                //String url = "http://ec2-54-196-152-211.compute-1.amazonaws.com:8081/publish/jobs";
                String url = "http://localhost:8081/publish/jobs";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<PublishedJobs> requestEntity = new HttpEntity<>(publishedJobs, headers);
                ResponseEntity<HttpStatus> responseEntity = restTemplate.exchange(url,
                        HttpMethod.POST, requestEntity, HttpStatus.class);
                HttpStatus status = responseEntity.getBody();

                int code = status.value();
                if (code == HttpStatus.BAD_REQUEST.value()) {
                    System.err.println("Error while adding new job as provided request is bad");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("BAD REQUEST");
                }
                if (code == HttpStatus.OK.value()) {
                    System.out.println("Published job to broker");
                    return ResponseEntity.status(HttpStatus.OK).body("Job Added");
                }
            } catch (Exception ex) {
                System.err.println("Exception occurred: " + ex.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Broker is down");
    }
}
