package com.jobnotification.service;


import com.jobnotification.model.PublishedJobs;
import com.jobnotification.model.SubscriberModel;
import org.springframework.http.HttpStatus;

import java.util.List;

public interface BrokerService {

    //static final String DOCKER_VOLUME = "opt/";
    static final String DOCKER_VOLUME = "/home/pranav/";
    static final String SUBSCRIBER_LIST = DOCKER_VOLUME + "subscriberlist.txt";
    static final String PUBLISHER_SUBSCRIBER_MAP = DOCKER_VOLUME + "pubsubmapping.txt";

    public HttpStatus subscribe(SubscriberModel subsriberModel);

    public HttpStatus publishJobsAndBroadCast(PublishedJobs publishedJobs);

    public List<String> getTopics();

    public HttpStatus addNewPublisher(String companyName);

    //File Structure ::
    //SUBSCRIBER_LIST :
    //  subsId,portNo
    //  subsId,portNo
    //PUBLISHER_SUBSCRIBER_LIST :
    //  compnyNmae-subsId1,subsId2
    //  compnyNmae-subsId1,subsId2
    //companyName_Jobs:
    //  jobId,jobTitle,locatn,desc
    //  jobId,jobTitle,locatn,desc
    //NoAck
    //  subsId-companyName_JobId1,companyName_JobId2

}
