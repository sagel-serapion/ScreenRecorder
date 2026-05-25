package com.project.screenrecorder.Service;


import com.project.screenrecorder.DTO.ChunkUploadResponse;
import com.project.screenrecorder.DTO.UploadInitRequest;
import com.project.screenrecorder.DTO.UploadInitResponse;
import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Entity.VideoChunk;
import com.project.screenrecorder.Mapper.VideoMapper;
import com.project.screenrecorder.Repository.VideoChunkRepository;
import com.project.screenrecorder.Repository.VideoRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final VideoMapper videoMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final VideoRepository videoRepository;

    private final VideoChunkRepository videoChunkRepository;

    private final MinioClient minioClient;


    @Value("${minio.buckets.chunks}")
    private String chunksBucket;

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

    public ChunkUploadResponse uploadChunk(String videoId , int chunkIndex , MultipartFile file){

        try {

            String objectName = videoId + "/" + chunkIndex;
            String minioPath = chunksBucket + "/" + objectName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(chunksBucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType("application/octet-stream")
                            .build()

            );

            VideoChunk chunk = new VideoChunk();
            chunk.setVideoId(videoId);
            chunk.setChunkIndex(chunkIndex);
            chunk.setMinioPath(minioPath);
            videoChunkRepository.save(chunk);

            ChunkUploadResponse response = new ChunkUploadResponse();
            response.setChunkIndex(chunkIndex);
            response.setReceived(true);

            return response;


        } catch (Exception e) {
            throw new RuntimeException("Failed to upload chunk" + e.getMessage());
        }




    }
}
