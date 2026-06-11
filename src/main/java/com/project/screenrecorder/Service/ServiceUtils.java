package com.project.screenrecorder.Service;


import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Exception.VideoNotFoundException;
import com.project.screenrecorder.Exception.VideoNotReadyException;
import com.project.screenrecorder.Repository.VideoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceUtils {

    private final VideoRepository videoRepository;


    protected Video resolveVideo(String token){

        Video video = videoRepository.findByToken(token).orElseThrow(
                () -> new VideoNotFoundException("Video with " + token + " not found")
        );

        if (video.getStatus() != Video.VideoStatus.READY){
            throw new VideoNotReadyException("Video with "+ token + " is still processing");
        }
        return video;
    }

    protected String getViewerIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
