package com.jobnotification.service;

import com.jobnotification.model.PublishedJobs;
import com.jobnotification.model.SubscriberModel;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PublishBroadcastService implements Callable<HttpStatus> {

    //static final String DOCKER_VOLUME = "opt/";
    static final String DOCKER_VOLUME = "/home/pranav/";
    static final String PUBLISHER_SUBSCRIBER_MAP = DOCKER_VOLUME + "pubsubmapping.txt";
    static final String NO_ACK_LIST = DOCKER_VOLUME + "NoAck.txt";
    SpanningTree spanningTree;
    private Lock lock;
    private final PublishedJobs currentPublishedJob;

    public PublishBroadcastService(PublishedJobs currentPublishedJob, SpanningTree spanningTree) {
        this.currentPublishedJob = currentPublishedJob;
        this.spanningTree = spanningTree;
        this.lock = new ReentrantLock();
    }

    @Override
    public HttpStatus call() throws Exception {
        HttpStatus status = HttpStatus.OK;
        String companyName = currentPublishedJob.getCompanyName();
        try {
            // writing job to the company's queue(file)
            lock.lock();
            Files.write(Paths.get(DOCKER_VOLUME + companyName + "_Jobs.txt"),
                    (currentPublishedJob.getJobId() + "," + currentPublishedJob.getJobTitle() + "," +
                            currentPublishedJob.getJobLocation() + "," + currentPublishedJob.getDescription()
                            + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            lock.unlock();

            Map<String, List<SubscriberModel>> pubSubNode = spanningTree.getPubSubNode();

            // call broadcast if the company has subscribers
            if (!CollectionUtils.isEmpty(pubSubNode.get(companyName))) {
                status = broadcastToSubscribers(pubSubNode.get(companyName), currentPublishedJob);
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            return status;
        }
        return status;
    }

    private HttpStatus broadcastToSubscribers(List<SubscriberModel> subscriberNodes, PublishedJobs currentPublishedJob) {
        try {
            File file = new File(NO_ACK_LIST);
            if (!file.exists()) {
                file.createNewFile();
            }
            String company = currentPublishedJob.getCompanyName();
            Map<String, List<String>> subsAckJobsMap = new HashMap<>();
            Map<String, List<String>> subsNoAckJobsMap = new HashMap<>();
            List<String> subscriberNoAckDetails = readLinesFromFile(NO_ACK_LIST);

            for (SubscriberModel subscriberNode : subscriberNodes) {
                List<PublishedJobs> jobsToPublish = new ArrayList<>();
                jobsToPublish.add(currentPublishedJob);

                //find other jobs to publish which had been unacknowledged earlier
                if (!CollectionUtils.isEmpty(subscriberNoAckDetails)) {
                    jobsToPublish = findAllJobsToPublish(company, subscriberNode, jobsToPublish);
                }

                for (PublishedJobs jobToPublish : jobsToPublish) {
                    String url = "http://" + subscriberNode.getAwsUrl() + ":" + subscriberNode.getPort() + "/notify";
                    System.out.println("Subscriber url to notify : " + url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<PublishedJobs> requestEntity = new HttpEntity<>(jobToPublish, headers);

                    System.out.println("Sending jobId " + jobToPublish.getJobId() + " to subscriber " +
                            subscriberNode.getSubscriberId() + " at port " + subscriberNode.getPort());
                    HttpStatus statusCode;
                    try {
                        ResponseEntity<HttpStatus> responseEntity = new RestTemplate().exchange(url,
                                HttpMethod.POST, requestEntity, HttpStatus.class);
                        statusCode = responseEntity.getBody();
                    } catch (Exception e) {
                        System.out.println("Error while notifying subscriber : " + e.getMessage());
                        statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                    }

                    if (!statusCode.equals(HttpStatus.OK)) {
                        System.out.println("No ACK for subscriber " + subscriberNode.getSubscriberId()
                                + " at port " + subscriberNode.getPort() + "for job Id " + jobToPublish.getJobId());

                        List<String> noAckJobs = subsNoAckJobsMap.get(String.valueOf(subscriberNode.getSubscriberId()));
                        String noAckJob = company + "_" + String.valueOf(jobToPublish.getJobId()) + ",";
                        if (CollectionUtils.isEmpty(noAckJobs)) {
                            subsNoAckJobsMap.put(String.valueOf(subscriberNode.getSubscriberId()),
                                    new ArrayList<String>(Arrays.asList(noAckJob)));
                        } else {
                            noAckJobs.add(noAckJob);
                            subsNoAckJobsMap.put(String.valueOf(subscriberNode.getSubscriberId()), noAckJobs);
                        }
                    } else {
                        System.out.println("Subscriber " + subscriberNode.getSubscriberId() +
                                " at port " + subscriberNode.getPort() + " acknowledged for job Id"
                                + jobToPublish.getJobId());

                        List<String> ackJobs = subsAckJobsMap.get(String.valueOf(subscriberNode.getSubscriberId()));
                        if (CollectionUtils.isEmpty(ackJobs)) {
                            String ackJob = company + "_" + String.valueOf(jobToPublish.getJobId()) + ",";
                            subsAckJobsMap.put(String.valueOf(subscriberNode.getSubscriberId()),
                                    new ArrayList<String>(Arrays.asList(ackJob)));
                        } else {
                            ackJobs.add(company + "_" + String.valueOf(jobToPublish.getJobId()) + ",");
                            subsAckJobsMap.put(String.valueOf(subscriberNode.getSubscriberId()), ackJobs);
                        }
                    }
                }

                //updating NoAck file & adding those which got No ack
                List<String> updatedNoAcks = new ArrayList<>();
                updatedNoAcks.addAll(subscriberNoAckDetails);
                List<String> existingEntries = new ArrayList<>();

                for (String subscriberNoAckDetail : subscriberNoAckDetails) {
                    String subscId = subscriberNoAckDetail.split("-")[0];
                    String toUpdate = subscriberNoAckDetail;
                    int needsUpdate = 0;
                    if (subsNoAckJobsMap.containsKey(subscId)) {
                        existingEntries.add(subscId);
                        List<String> subsNoAckJobs = subsNoAckJobsMap.get(subscId);
                        for (String subsNoAckJob : subsNoAckJobs) {
                            if (!subscriberNoAckDetail.contains(subsNoAckJob)) {
                                needsUpdate = 1;
                                toUpdate = toUpdate + subsNoAckJob;
                            }
                        }
                    }
                    if (needsUpdate == 1) {
                        updatedNoAcks.add(toUpdate);
                        updatedNoAcks.remove(subscriberNoAckDetail);
                    }
                }
                for (Map.Entry<String, List<String>> map : subsNoAckJobsMap.entrySet()) {
                    if (!existingEntries.contains(map.getKey())) {
                        String newEntry = map.getKey() + "-";
                        for (String str : map.getValue()) {
                            newEntry = newEntry + str;
                        }
                        updatedNoAcks.add(newEntry);
                    }
                }

                //updating NoAck file removing those which got acknowledgement
                List<String> newNoAcks = new ArrayList<>();
                newNoAcks.addAll(updatedNoAcks);

                if (!CollectionUtils.isEmpty(updatedNoAcks)) {
                    for (String subscriberNoAckDetail : updatedNoAcks) {
                        String subscId = subscriberNoAckDetail.split("-")[0];
                        int needsUpdate = 0;
                        if (subscId != "" && subsAckJobsMap.containsKey(subscId)) {
                            String updatedNoAck = subscriberNoAckDetail;
                            List<String> ackJobs = subsAckJobsMap.get(subscId);
                            for (String ackJob : ackJobs) {
                                if (subscriberNoAckDetail.contains(ackJob)) {
                                    needsUpdate = 1;
                                    updatedNoAck = updatedNoAck.replace(ackJob, "");
                                }
                            }
                            if (needsUpdate == 1) {
                                newNoAcks.remove(subscriberNoAckDetail);
                                newNoAcks.add(updatedNoAck);
                            }
                        }
                    }
                }

                lock.lock();
                Files.write(Path.of(NO_ACK_LIST), newNoAcks);
                lock.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.OK;
    }


    private List<PublishedJobs> findAllJobsToPublish(String companyName, SubscriberModel subcriberNode,
                                                     List<PublishedJobs> jobsToPublish) {

        //companyName_Jobs:
        //  jobId,jobTitle,locatn,desc
        //  jobId,jobTitle,locatn,desc
        List<String> publishedJobsDetails = readLinesFromFile(DOCKER_VOLUME + companyName + "_Jobs.txt");

        //NoAck
        //  subsId-companyName_JobId1,companyName_JobId2
        List<String> subsriberNoAckDetails = readLinesFromFile(NO_ACK_LIST);

        for (String subscriberAckDetail : subsriberNoAckDetails) { //iterating lines of file NoAck
            if (!subscriberAckDetail.split("-")[0].equals("") && Integer.parseInt(subscriberAckDetail.split("-")[0]) == subcriberNode.getSubscriberId()
                    && subscriberAckDetail.split("-").length > 1) {
                String noAckDetail = subscriberAckDetail.split("-")[1]; //companyName_JobId1,companyName_JobId2
                String[] company_JobIds = noAckDetail.split(","); //companyName_JobId1
                for (String company_JobId : company_JobIds) {
                    if (company_JobId.split("_")[0].equals(companyName)) {
                        String jobId = company_JobId.split("_")[1];
                        for (String publishedJob : publishedJobsDetails) {  //iterating lines of file companyName_Jobs
                            String[] jobDetails = publishedJob.split(","); //jobId,jobTitle,locatn,desc
                            if (jobDetails[0].equals(jobId)) {
                                PublishedJobs jobToPublish = new PublishedJobs();
                                jobToPublish.setJobId(Integer.parseInt(jobId));
                                jobToPublish.setJobTitle(jobDetails[1]);
                                jobToPublish.setJobLocation(jobDetails[2]);
                                jobToPublish.setDescription(jobDetails[3]);
                                jobToPublish.setCompanyName(companyName);
                                jobsToPublish.add(jobToPublish);
                            }
                        }
                    }
                }
                break;
            }
        }

        return jobsToPublish;

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
