package com.jobrecruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobRecruitmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobRecruitmentApplication.class, args);
    }
}
