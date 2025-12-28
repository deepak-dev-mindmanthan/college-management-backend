package org.collegemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class.
 * 
 * @EnableScheduling enables scheduled jobs for subscription renewal automation.
 */
@SpringBootApplication
@EnableScheduling
public class CollegeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollegeManagementApplication.class, args);
    }

}
