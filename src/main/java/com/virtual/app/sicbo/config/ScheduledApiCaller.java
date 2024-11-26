package com.virtual.app.sicbo.config;


import com.virtual.app.sicbo.module.services.impl.SicBoEvoApiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class ScheduledApiCaller {

    Properties config = new Properties();
    private final SicBoEvoApiService apiService;

    public ScheduledApiCaller(SicBoEvoApiService apiService) {
        this.apiService = apiService;
    }

    @Scheduled(fixedRate = 3000) // Every 1000ms (1 second)
    public void scheduleApiCall() {




        try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
            config.load(input);
            boolean isSicBoEnabled = Boolean.parseBoolean(config.getProperty("game.sicbo", "false"));
            if (isSicBoEnabled) {
                apiService.fetchData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
