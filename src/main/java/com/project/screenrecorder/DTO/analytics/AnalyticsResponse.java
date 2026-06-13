package com.project.screenrecorder.DTO.analytics;


import lombok.Data;
import java.util.Map;

@Data
public class AnalyticsResponse {

        private int totalViews;

        private int averageWatchDuration;

        private String dropOffPoint;

        private Map<String,Integer> dropOffBreakdown;

}
