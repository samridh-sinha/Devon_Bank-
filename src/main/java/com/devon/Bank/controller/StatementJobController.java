package com.devon.Bank.controller;

import com.devon.Bank.dto.StatementJobResult;
import com.devon.Bank.service.StatementGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class StatementJobController {

    private final StatementGenerationService statementService;

    public StatementJobController(StatementGenerationService statementService) {
        this.statementService = statementService;
    }

    @PostMapping("/generate-monthly-statements")
    public ResponseEntity<String> generateMonthlyStatements(@RequestParam int year, @RequestParam int month) {
        StatementJobResult result = statementService.generateForMonth(year, month);
        return ResponseEntity.status(202).body("Statement generation job started");
    }
}
