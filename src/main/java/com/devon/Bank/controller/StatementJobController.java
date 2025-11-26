package com.devon.Bank.controller;

import com.devon.Bank.configuration.JobStore;
import com.devon.Bank.model.JobStatus;
import com.devon.Bank.service.StatementGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class StatementJobController {

    private final StatementGenerationService statementService;
    private final JobStore jobStore;


    public StatementJobController(StatementGenerationService statementService, JobStore jobStore) {
        this.statementService = statementService;
        this.jobStore = jobStore;
    }

    @PostMapping("/generate-monthly-statements")
    public ResponseEntity<String> generateMonthlyStatements(@RequestParam int year, @RequestParam int month) {
        String jobId = statementService.submitJob(year, month);
        return ResponseEntity.accepted().body(
                "Job submitted. Track at /jobs/status/" + jobId
        );
    }


    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatus> getStatus(@PathVariable String jobId) {
        JobStatus status = jobStore.get(jobId);
        return status != null ?
                ResponseEntity.ok(status) :
                ResponseEntity.notFound().build();
    }
}
