package com.jobnotification.controller;

import com.google.gson.Gson;
import com.jobnotification.model.PublishedJobs;
import com.jobnotification.model.SubscriberModel;
import com.jobnotification.service.SubscriberServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin("*")
@RestController
public class SubscriberController {

    @Autowired
    SubscriberServiceImpl subscriberService;

    @GetMapping(value = "/get/topics")
    public ResponseEntity<String> getTopics() {
        StringBuilder response = new StringBuilder();
        try {
            //URL url = new URL("http://ec2-54-196-152-211.compute-1.amazonaws.com:8081/get/topics");
            URL url = new URL("http://localhost:8081/get/topics");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            System.out.println("Response body: " + response.toString());
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] topics = response.toString().split(",");
        for (int i = 0; i < topics.length; i++) {
            topics[i] = topics[i].replaceAll("[^a-zA-Z0-9]", "");
        }

        List<String> str = new ArrayList<>(Arrays.asList(topics));
        String json = new Gson().toJson(str);
        System.out.println(json);
        return ResponseEntity.ok().body(json);
    }

    @PostMapping(value = "/subscribe")
    public String subscribe(@RequestBody SubscriberModel subscriberModel) {
        HttpStatus httpStatus = subscriberService.subscribe(subscriberModel);
        System.out.println(httpStatus.value());
        return String.valueOf(Integer.parseInt(String.valueOf(httpStatus.value())));
    }

    @PostMapping(value = "/notify")
    public ResponseEntity<HttpStatus> notify(@RequestBody PublishedJobs publishedJobs) {
        HttpStatus status = subscriberService.notify(publishedJobs);
        //Acknowledgement
        return ResponseEntity.ok().body(status);
    }

    @GetMapping(value = "/jobs")
    public List<PublishedJobs> getAllJobs() {
        List<PublishedJobs> publishedJobsList = new ArrayList<>();
        //String fileName = "opt/jobs.txt";
        String fileName = "/home/pranav/jobs.txt";

        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("No jobs posted yet");
                return new ArrayList<>();
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return new ArrayList<>();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 45,Software engineer,CA,This is job desc for 45,Apple
                String[] jobDetails = line.split(",");
                PublishedJobs publishedJob = new PublishedJobs();
                publishedJob.setJobId(Integer.parseInt(jobDetails[0]));
                publishedJob.setJobTitle(jobDetails[1]);
                publishedJob.setJobLocation(jobDetails[2]);
                publishedJob.setDescription(jobDetails[3]);
                publishedJob.setCompanyName(jobDetails[4]);
                publishedJobsList.add(publishedJob);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return publishedJobsList;
    }
}
