package com.interview.questionbank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.interview.questionbank.mapper")
public class InterviewQuestionBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterviewQuestionBankApplication.class, args);
    }
}
