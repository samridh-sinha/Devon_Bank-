package com.devon.Bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public String test() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM dual", Integer.class);
            return "DB Connected! Result = " + result;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    @GetMapping("/test-data")
    public List<Map<String, Object>> testData() {
        return jdbcTemplate.queryForList(
                "SELECT * FROM TRANSACTION_DATA FETCH FIRST 10 ROWS ONLY"
        );
    }
}