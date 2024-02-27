package com.jobnotification.controller;

import com.jobnotification.model.PublishedJobs;
import com.jobnotification.model.SubscriberModel;
import com.jobnotification.service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
public class BrokerController {

    @Autowired
    BrokerService brokerServiceImpl;

    @PostMapping(value = "/subscribe")
    public ResponseEntity<HttpStatus> subscribe(@RequestBody SubscriberModel subscriberModel) {
        HttpStatus ststus = brokerServiceImpl.subscribe(subscriberModel);
        System.out.println("ststus value " + ststus.value());
        return ResponseEntity.ok().body(ststus);

    }
    @PostMapping(value = "/add/publisher")
    public ResponseEntity<HttpStatus> addpublisher(@RequestBody String companyName) {
        HttpStatus ststus = brokerServiceImpl.addNewPublisher(companyName);
        System.out.println("Stsus " + ststus);
        System.out.println("ststus value " + ststus.value());
        return ResponseEntity.ok().body(ststus);

    }
    @GetMapping(value = "/get/topics")
    public ResponseEntity<List<String>> getTopics() {
        List<String> topicList = brokerServiceImpl.getTopics();
        return ResponseEntity.ok().body(topicList);
    }
    @PostMapping(value = "/publish/jobs")
    public ResponseEntity<HttpStatus> publishJobs(@RequestBody PublishedJobs newlyPublishedJob) {
        HttpStatus status = brokerServiceImpl.publishJobsAndBroadCast(newlyPublishedJob);
        return ResponseEntity.ok().body(status);
    }


}
