package com.jobnotification.service;

import com.jobnotification.model.PublishedJobs;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.jobnotification.model.SubscriberModel;


public interface SubsriberService {

    public HttpStatus subscribe(SubscriberModel subsriberModel) ;

    public HttpStatus notify (PublishedJobs publishedJobs);



}
