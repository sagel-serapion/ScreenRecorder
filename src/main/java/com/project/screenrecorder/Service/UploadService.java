package com.project.screenrecorder.Service;


import com.project.screenrecorder.DTO.ChunkUploadResponse;
import com.project.screenrecorder.DTO.UploadInitRequest;
import com.project.screenrecorder.DTO.UploadInitResponse;
import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Mapper.VideoMapper;
import com.project.screenrecorder.Repository.VideoChunkRepository;
import com.project.screenrecorder.Repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final VideoMapper videoMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final VideoRepository videoRepository;

    private final VideoChunkRepository videoChunkRepository;

    public  UploadInitResponse initUpload( UploadInitRequest uploadInitRequest){

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

  //  public ChunkUploadResponse uploadChunk(String videoId , int chunkIndex){




  //  }
}
