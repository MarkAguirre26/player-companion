package com.virtual.app.sicbo.config;


import com.virtual.app.sicbo.module.services.impl.SicBoEvoApiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledApiCaller {

    private final SicBoEvoApiService apiService;

    public ScheduledApiCaller(SicBoEvoApiService apiService) {
        this.apiService = apiService;
    }

    @Scheduled(fixedRate = 3000) // Every 1000ms (1 second)
    public void scheduleApiCall() {
        apiService.fetchData();
    }
}
