package com.edenrump.models.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricBlock {

    private String title;

    private Map<String, String> metrics = new HashMap<>();
    private List<String> metricKeys = new ArrayList<>();

    public MetricBlock(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getMetricValue(String metricTitle) {
        return metrics.getOrDefault(metricTitle, "No value");
    }

    public List<String> getMetricKeys(){
        return metricKeys;
    }

    public void addMetric(String metricTitle, String metricResult){
        metricKeys.add(metricTitle);
        metrics.put(metricTitle, metricResult);
    }

    public void removeMetric(String metricTitle){
        metricKeys.remove(metricTitle);
        metrics.remove(metricTitle);
    }
}
