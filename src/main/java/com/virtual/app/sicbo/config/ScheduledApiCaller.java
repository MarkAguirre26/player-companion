package com.virtual.app.sicbo.config;


import com.virtual.app.sicbo.module.services.impl.SicBoEvoApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class ScheduledApiCaller {
    private final SicBoEvoApiService apiService;

    @Value("${game.sicbo}")
    public String gameSicbo;

    // Constructor-based Dependency Injection
    public ScheduledApiCaller(SicBoEvoApiService apiService) {
        this.apiService = apiService;
    }

    // Scheduled method to call API every 3000ms (3 seconds)
    @Scheduled(fixedRate = 3000)
    public void scheduleApiCall() {
        boolean isSicBoEnabled = Boolean.parseBoolean(gameSicbo);
        if (isSicBoEnabled) {
            apiService.fetchData();
        }
    }
}
