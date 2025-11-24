package com.devon.Bank.controller;

import com.devon.Bank.service.HighValueMonitorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final HighValueMonitorService highValueMonitorService;

    public JobController(HighValueMonitorService highValueMonitorService) {
        this.highValueMonitorService = highValueMonitorService;
    }

    @PostMapping("/run-high-value-monitor")
    public String runHighValue(@RequestParam(required = false) String date) {
        LocalDate d = (date == null) ? LocalDate.now().minusDays(1) : LocalDate.parse(date);
        highValueMonitorService.runForDate(d);
        return "Triggered high-value monitor for " + d;
    }

}
