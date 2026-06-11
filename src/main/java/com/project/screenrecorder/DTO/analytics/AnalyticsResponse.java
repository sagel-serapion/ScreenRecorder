package com.project.screenrecorder.DTO.analytics;


import lombok.Data;

@Data
public class AnalyticsResponse {

        private int totalViews;

        private int averageWatchDuration;

        private int dropOffPoint;

}
