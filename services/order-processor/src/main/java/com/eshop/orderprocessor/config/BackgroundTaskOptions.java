package com.eshop.orderprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "background-task")
public class BackgroundTaskOptions {

    /**
     * Grace period time in minutes before an order is confirmed.
     */
    private int gracePeriodTime = 1;

    /**
     * How often (in seconds) to check for orders past grace period.
     */
    private int checkUpdateTime = 30;

    public int getGracePeriodTime() {
        return gracePeriodTime;
    }

    public void setGracePeriodTime(int gracePeriodTime) {
        this.gracePeriodTime = gracePeriodTime;
    }

    public int getCheckUpdateTime() {
        return checkUpdateTime;
    }

    public void setCheckUpdateTime(int checkUpdateTime) {
        this.checkUpdateTime = checkUpdateTime;
    }
}
