package com.jobnotification.service;

import com.jobnotification.model.PublishedJobs;
import org.springframework.http.ResponseEntity;

public interface PublisherService {
    public ResponseEntity<String> addPublisher(String companyName);

    public ResponseEntity<String> addJobs(PublishedJobs publishedJobs);
}
