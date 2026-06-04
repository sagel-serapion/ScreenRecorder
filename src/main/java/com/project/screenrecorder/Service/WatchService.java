package com.project.screenrecorder.Service;


import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Exception.VideoNotFoundException;
import com.project.screenrecorder.Exception.VideoNotReadyException;
import com.project.screenrecorder.Repository.VideoRepository;
import com.project.screenrecorder.Security.JwtUtils;
import com.project.screenrecorder.Security.SecurityBridge;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class WatchService {

    @Value("${minio.buckets.videos}")
    private String finalVideos;



    private final VideoRepository videoRepository;

    private final MinioClient minioClient;

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

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

        Video video = videoRepository.findById(videoId).orElseThrow(
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
            throw new RuntimeException("Temporary direct-access URL to MinIO failed " + e.getMessage());
        }
    }

    public String authenticateVideo(String token , String rawPassword){

        Authentication authentication = authenticationManager.authenticate(
                // passport
                new UsernamePasswordAuthenticationToken(
                        token,
                        rawPassword
                )
        );

        SecurityBridge securityBridge = (SecurityBridge) authentication.getPrincipal();


        return jwtUtils.generateToken(securityBridge.getUsername());



    }

    public String getWatchUrl(String token){
        Video video =  resolveVideo(token);

        if (video.getPasswordHash() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "This video is password protected, use /auth");
        }
        return generatePresignedUrl(video.getId());
    }

    public String getWatchUrl(String token , SecurityBridge current){

        if (!token.equals(current.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token mismatch");
        }
        return generatePresignedUrl(current.getVideoId());

    }
}