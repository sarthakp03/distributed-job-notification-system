package com.jobnotification.model;

import java.util.List;

public class SubscriberModel {

    int subscriberId;

    int port;

    String awsUrl ;

    List<String> companyNames;

    public int getSubscriberId() {
        return subscriberId;
    }

    public String getAwsUrl() {
        return awsUrl;
    }

    public void setAwsUrl(String awsUrl) {
        this.awsUrl = awsUrl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public List<String> getCompanyNames() {
        return companyNames;
    }

    public void setCompanyNames(List<String> companyNames) {
        this.companyNames = companyNames;
    }
}