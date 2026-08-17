package com.conexa.starwars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JavaChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaChallengeApplication.class, args);
    }

}
