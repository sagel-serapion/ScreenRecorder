package com.project.screenrecorder.Service;


import com.project.screenrecorder.DTO.UploadInitRequest;
import com.project.screenrecorder.DTO.UploadInitResponse;
import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Mapper.VideoMapper;
import com.project.screenrecorder.Repository.VideoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final VideoMapper videoMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final VideoRepository videoRepository;

    private UploadInitResponse initUpload(@Valid @RequestBody UploadInitRequest uploadInitRequest){

        Video video = videoMapper.toEntity(uploadInitRequest);
        video.setToken(UUID.randomUUID().toString());
        video.setStatus(Video.VideoStatus.UPLOADING);

        if (uploadInitRequest.isHasPassword() && uploadInitRequest.getPassword() != null  ){
            video.setPasswordHash(passwordEncoder.encode(uploadInitRequest.getPassword()));
        }

        Video saved = videoRepository.save(video);

        UploadInitResponse response = new UploadInitResponse();

        response.setVideoId(saved.getId());


        return response ;
    }
}
