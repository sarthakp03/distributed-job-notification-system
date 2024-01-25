package com.jobnotification.controller;

import com.jobnotification.model.PublishedJobs;
import com.jobnotification.service.PublisherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
public class PublisherController {

    @Autowired
    PublisherServiceImpl publisherServiceImpl;

    @PostMapping(value = "/add/publisher")
    public ResponseEntity<String> addPublisher(@RequestBody String companyName) {
        System.out.println("Company name: " + companyName);
        ResponseEntity<String> result = publisherServiceImpl.addPublisher(companyName);
        return result;
    }

    @PostMapping(value = "/publish/jobs")
    public ResponseEntity<String> addJobs(@RequestBody PublishedJobs publishedJobs) {
        return publisherServiceImpl.addJobs(publishedJobs);
    }
}
