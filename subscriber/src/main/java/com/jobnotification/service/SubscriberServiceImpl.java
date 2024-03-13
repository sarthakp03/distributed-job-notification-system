package com.jobnotification.service;

import com.jobnotification.model.PublishedJobs;
import com.jobnotification.model.SubscriberModel;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class SubscriberServiceImpl implements SubsriberService {

    @Autowired
    RestTemplate restTemplate;

    @Override
    public HttpStatus subscribe(SubscriberModel subsriberModel) {
        System.out.println("=======Subscriber details at broker====");
        System.out.println("Subs id "+ subsriberModel.getSubscriberId());
        System.out.println("Subs port "+ subsriberModel.getPort());
        System.out.println(subsriberModel.getCompanyNames().toString());
        //String url = "http://ec2-54-196-152-211.compute-1.amazonaws.com:8081/subscribe";
        String url = "http://localhost:8081/subscribe";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubscriberModel> req = new HttpEntity<>(subsriberModel, headers);
        ResponseEntity<HttpStatus> responseEntity = restTemplate.exchange(url,
                HttpMethod.POST, req, HttpStatus.class);

        return responseEntity.getBody();
    }

    @Override
    public HttpStatus notify(PublishedJobs publishedJobs) {
        System.out.println("=============================================");
        System.out.println("Company : " + publishedJobs.getCompanyName());
        System.out.println("Job Id : " + publishedJobs.getJobId());
        System.out.println("Title : " + publishedJobs.getJobTitle());
        System.out.println("Location : " + publishedJobs.getJobLocation());
        System.out.println("Description : " + publishedJobs.getDescription());
        System.out.println("=============================================");

        //File file = new File("opt/jobs.txt");
        File file = new File("/home/pranav/jobs.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            //Files.write(Paths.get("opt/jobs.txt"),
            Files.write(Paths.get("/home/pranav/jobs.txt"),
                    (publishedJobs.getJobId() + "," +
                            publishedJobs.getJobTitle() + "," +
                            publishedJobs.getJobLocation() + "," +
                            publishedJobs.getDescription() + "," +
                            publishedJobs.getCompanyName() +
                            System.lineSeparator()).getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return HttpStatus.OK;
    }
}
