package com.project.screenrecorder.Service;


import com.project.screenrecorder.DTO.analytics.AnalyticsResponse;
import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Entity.WatchSession;
import com.project.screenrecorder.Exception.VideoNotFoundException;
import com.project.screenrecorder.Repository.WatchSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RedisTemplate redisTemplate;

    private final WatchSessionRepository watchSessionRepository;


    private final ServiceUtils serviceUtils;



    public void startSession(String videoId , String viewerIp){

        String key = "session:" + videoId + ":" + viewerIp;

        LocalDateTime  now = LocalDateTime.now();
        String nowStr = now.toString();

        redisTemplate.opsForHash().put(key,"startedAt" ,nowStr);
        redisTemplate.opsForHash().put(key,"lastPosition","0");
        redisTemplate.opsForHash().put(key,"lastSeenAt",nowStr);
        redisTemplate.expire(key,30, TimeUnit.MINUTES);

        WatchSession session = new WatchSession();
        session.setVideoId(videoId);
        session.setViewerIp(viewerIp);
        session.setStartedAt(now);
        session.setWatchDuration(0L);

        watchSessionRepository.save(session);



    }

    public void  recordPing(String token , HttpServletRequest request , int position){

        Video video = serviceUtils.resolveVideo(token);
        String viewerIp = request.getRemoteAddr();
        String videoId = video.getId();
        String key = "session:" + videoId + ":" + viewerIp;

        if (!redisTemplate.hasKey(key)){
            startSession(videoId,viewerIp);
        }
        redisTemplate.expire(key,30, TimeUnit.MINUTES);

        int currentLastPosition = Integer.parseInt(
                (String) redisTemplate.opsForHash().get(key, "lastPosition")
        );



        if (position > currentLastPosition) {
            String lastPosition = String.valueOf(position);
            redisTemplate.opsForHash().put(key,"lastPosition",lastPosition);

            LocalDateTime lastSeenAt = LocalDateTime.now();
            String lastSeenAtStr= lastSeenAt.toString();
            redisTemplate.opsForHash().put(key,"lastSeenAt",lastSeenAtStr);



            WatchSession watchSession = watchSessionRepository.findTopByVideoIdAndViewerIpOrderByStartedAtDesc(videoId,viewerIp)
                    .orElseThrow(() -> new VideoNotFoundException("watchSession object not found"));

            watchSession.setLastPosition(position);
            watchSession.setLastSeenAt(lastSeenAt);
            Long watchDuration = Duration.between(watchSession.getStartedAt(), lastSeenAt).getSeconds();
            watchSession.setWatchDuration(watchDuration);
            watchSessionRepository.save(watchSession);

        }

    }

    public AnalyticsResponse getAnalytics(String token){

        Video video = serviceUtils.resolveVideo(token);



        List<WatchSession> watchSessions = watchSessionRepository.findByVideoId(video.getId());
        int totalViews = watchSessions.size();

        int totalWatchDuration = 0;

        Map<String,Integer> buckets = new HashMap<>();


        for (WatchSession watchSession :watchSessions ){
            totalWatchDuration += watchSession.getWatchDuration();

            int bucketStart = (int)(Math.floor(watchSession.getLastPosition() / 10.0) * 10);
            String key = bucketStart + "-" + (bucketStart + 10) + "s";
            buckets.put(key,buckets.getOrDefault(key,0) + 1);


        }

        String dropOffPoint = buckets.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        int avgWatchDuration  = totalViews > 0 ? totalWatchDuration / totalViews : 0;

        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalViews(totalViews);
        response.setDropOffPoint(dropOffPoint);
        response.setAverageWatchDuration(avgWatchDuration);
        response.setDropOffBreakdown(buckets);
        return response;


    }









}
