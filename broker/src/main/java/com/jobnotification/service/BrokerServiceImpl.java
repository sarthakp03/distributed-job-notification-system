package com.jobnotification.service;

import com.jobnotification.model.PublishedJobs;
import com.jobnotification.model.SubscriberModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BrokerServiceImpl implements BrokerService {
    @Autowired
    RestTemplate restTemplate;
    SpanningTree spanningTree;
    private Lock lock;
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    //to initialise the spanning tree with existing publisher and subscriber details if any
    @PostConstruct
    public void init() {
        System.out.println("Executing init ..");
        this.lock = new ReentrantLock();

        File file = new File(PUBLISHER_SUBSCRIBER_MAP);
        if (!file.exists()) {
            spanningTree = new SpanningTree();
        } else {
            spanningTree = new SpanningTree();
            List<String> pubSubMapping = readLinesFromFile(PUBLISHER_SUBSCRIBER_MAP);
            for (String line : pubSubMapping) {
                List<SubscriberModel> subscriberList = new ArrayList<>();
                String[] pubSub = line.split("-");
                if (pubSub[1] != null || !pubSub[1].isEmpty() || !pubSub[1].equals("")) {
                    String[] subscribers = pubSub[1].split(",");
                    List<String> subsriberDetails = readLinesFromFile(SUBSCRIBER_LIST);
                    for (String subsDetails : subsriberDetails) {
                        if (Arrays.stream(subscribers).anyMatch(subsDetails.split(",")[0]::contains)) {
                            SubscriberModel subscriberModel = new SubscriberModel();
                            String[] subscriberDetail = subsDetails.split(",");
                            subscriberModel.setSubscriberId(Integer.parseInt(subscriberDetail[0]));
                            subscriberModel.setPort(Integer.parseInt(subscriberDetail[1]));
                            subscriberModel.setAwsUrl(subscriberDetail[2]);
                            subscriberList.add(subscriberModel);
                        }
                    }
                }
                spanningTree.getPubSubNode().put(pubSub[0], subscriberList);
            }
        }

    }


    //if PUBLISHER_SUBSCRIBER_MAP not created it creates and adds publishername to it ; else
    //it appends to the existing file in the form "companyName-"
    @Override
    public HttpStatus addNewPublisher(String companyName) {

        String[] body = companyName.split(":");
        companyName = body[1].replaceAll("[^a-zA-Z0-9]", "");
        System.out.println("Publisher name  " + companyName);

        HttpStatus status = HttpStatus.OK;
        try {
            lock.lock();
            Files.write(Paths.get(PUBLISHER_SUBSCRIBER_MAP),
                    (companyName + "-" + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            lock.unlock();

            //Add node in spanning tree
            List<SubscriberModel> subscriberList = new ArrayList<>();
            spanningTree.getPubSubNode().put(companyName, subscriberList);

        } catch (IOException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return status;
    }


    @Override
    public HttpStatus subscribe(SubscriberModel subsriberModel) {
        HttpStatus status = HttpStatus.OK;
        try {
            File file = new File(SUBSCRIBER_LIST);
            if (!file.exists()) {
                file.createNewFile();
            }

            //add subriberid and port number to subscriber list if not already added
            boolean subscriberExists = false;
            List<String> existingSubscriberList = Files.readAllLines(Paths.get(SUBSCRIBER_LIST));
            if (!CollectionUtils.isEmpty(existingSubscriberList)) {
                for (String subscriber : existingSubscriberList) {
                    if (subscriber.split(",")[0].equals(String.valueOf(subsriberModel.getSubscriberId()))) {
                        subscriberExists = true;
                        break;
                    }
                }
            }
            lock.lock();
            if (!subscriberExists) {
                Files.write(Paths.get(SUBSCRIBER_LIST), (subsriberModel.getSubscriberId() + "," +
                        subsriberModel.getPort() + "," + subsriberModel.getAwsUrl() +
                        System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
            lock.unlock();

            System.out.println("=======Subscriber details at broker====");
            System.out.println("Subscriber id " + subsriberModel.getSubscriberId());
            System.out.println("Subscriber port " + subsriberModel.getPort());
            System.out.println("Subscriber awsUrl " + subsriberModel.getAwsUrl());
            System.out.println("Subscribed companies " + subsriberModel.getCompanyNames().toString());

            List<String> companyNames = subsriberModel.getCompanyNames();
            // sadd subscriber to the Publisher-Subscriber mapping list ;
            // this list maintains the subribers corresponding to each publisher
            BufferedReader bufferedReader = new BufferedReader(new FileReader(PUBLISHER_SUBSCRIBER_MAP));
            StringBuilder updatedMapping = new StringBuilder();
            String pubSubMapping;
            while ((pubSubMapping = bufferedReader.readLine()) != null) {
                if (companyNames.stream().anyMatch(pubSubMapping::contains)) {
                    pubSubMapping += subsriberModel.getSubscriberId() + ",";
                }
                updatedMapping.append(pubSubMapping).append("\n");
            }
            bufferedReader.close();

            lock.lock();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(PUBLISHER_SUBSCRIBER_MAP));
            bufferedWriter.write(updatedMapping.toString());
            bufferedWriter.close();
            lock.unlock();

            // Add to spannning tree
            for (String company : subsriberModel.getCompanyNames()) {
                List<SubscriberModel> subscList = spanningTree.getPubSubNode().get(company);
                subscList.add(subsriberModel);
                spanningTree.getPubSubNode().put(company, subscList);
            }
            System.out.println("Pub-Sub mapping updated after subscribe.");
        } catch (IOException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            return status;
        }
        return status;
    }


    //publish and broadcast
    @Override
    public HttpStatus publishJobsAndBroadCast(PublishedJobs currentPublishedJob) {
        HttpStatus httpStatus = HttpStatus.OK;
        Future<HttpStatus> future = threadPoolTaskExecutor.submit
                (new PublishBroadcastService(currentPublishedJob, spanningTree));
        try {
            httpStatus = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Exception in publish and broadcast " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    @Override
    public List<String> getTopics() {
        List<String> pubSubMapping = readLinesFromFile(PUBLISHER_SUBSCRIBER_MAP);
        List<String> topicList = new ArrayList<>();

        for (String pubSubMap : pubSubMapping) {
            topicList.add(pubSubMap.split("-")[0]);
        }
        return topicList;
    }

    private static List<String> readLinesFromFile(String filePath) {
        List<String> linesOfFile = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                linesOfFile = Files.readAllLines(Path.of(filePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linesOfFile;
    }
}
