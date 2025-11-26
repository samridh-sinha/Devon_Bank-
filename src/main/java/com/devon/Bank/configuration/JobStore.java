package com.devon.Bank.configuration;

import com.devon.Bank.model.JobStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JobStore {

    private final Map<String, JobStatus> store = new HashMap<>();

    public void save(JobStatus status) {
        store.put(status.getJobId(), status);
    }

    public JobStatus get(String jobId) {
        return store.get(jobId);
    }
}
