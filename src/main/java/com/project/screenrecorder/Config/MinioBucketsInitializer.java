package com.project.screenrecorder.Config;


import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MinioBucketsInitializer {


    @Value("${minio.buckets.chunks}")
    private String chunksBucket;

    @Value("${minio.buckets.videos}")
    private String videosBucket;

    @Value("${minio.buckets.thumbnails}")
    private String thumbnailsBucket;

    private final MinioClient minioClient;

    @PostConstruct
    public void createBucketsIfNotExist() {

        try{
            createBucket(chunksBucket);
            createBucket(videosBucket);
            createBucket(thumbnailsBucket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO buckets",e);
        }

    }

    private void createBucket(String bucketname) throws Exception {

        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketname).build()
        );

        if (!exists){
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketname).build()
            );
        }

    }
}
