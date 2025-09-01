package com.projet.freelencetinder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Active @Scheduled pour CaptureRetryScheduler & PayoutRetryScheduler
}
