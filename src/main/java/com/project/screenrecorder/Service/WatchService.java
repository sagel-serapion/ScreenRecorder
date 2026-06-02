package com.project.screenrecorder.Service;


import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Exception.VideoNotFoundException;
import com.project.screenrecorder.Exception.VideoNotReadyException;
import com.project.screenrecorder.Repository.VideoRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class WatchService {

    @Value("${minio.buckets.videos}")
    private String finalVideos;

    private final VideoRepository videoRepository;

    private final MinioClient minioClient;

    private Video resolveVideo(String token) {

            Video video = videoRepository.findByToken(token).orElseThrow(
                    () -> new VideoNotFoundException("Video with " + token + " not found")
            );

            if (video.getStatus() != Video.VideoStatus.READY){
                throw new VideoNotReadyException("Video with "+ token + " is still processing");
            }

            return video;
    }

    private String generatePresignedUrl(String videoId){

        Video video = videoRepository.findByVideoId(videoId).orElseThrow(
                () -> new VideoNotFoundException("Video with " + videoId + " not found")
        );

        try {

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(finalVideos)
                            .object(video.getMinioPath())
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Temporary direct-access URL to MinIO failed" + e.getMessage());
        }
    }









}