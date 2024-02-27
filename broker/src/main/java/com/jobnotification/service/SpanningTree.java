package com.jobnotification.service;

import com.jobnotification.model.SubscriberModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpanningTree {
    Map<String, List<SubscriberModel>> pubSubNode = new ConcurrentHashMap<>();

    public Map<String, List<SubscriberModel>> getPubSubNode() {
        return pubSubNode;
    }

    public void setPubSubNode(Map<String, List<SubscriberModel>> pubSubNode) {
        this.pubSubNode = pubSubNode;
    }
}
