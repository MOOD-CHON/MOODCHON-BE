package com.example.moodchon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoodchonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoodchonApplication.class, args);
    }

}
