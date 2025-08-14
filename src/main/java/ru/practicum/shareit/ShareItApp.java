package ru.practicum.shareit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareItApp {
    private static final Logger log = LoggerFactory.getLogger(ShareItApp.class);

    public static void main(String[] args) {
        SpringApplication.run(ShareItApp.class, args);
    }

}
