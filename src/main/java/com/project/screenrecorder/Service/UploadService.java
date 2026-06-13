package com.project.screenrecorder.Service;


import com.project.screenrecorder.DTO.upload.ChunkUploadResponse;
import com.project.screenrecorder.DTO.upload.CompleteUploadResponse;
import com.project.screenrecorder.DTO.upload.UploadInitRequest;
import com.project.screenrecorder.DTO.upload.UploadInitResponse;
import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Entity.VideoChunk;
import com.project.screenrecorder.Mapper.VideoMapper;
import com.project.screenrecorder.Repository.VideoChunkRepository;
import com.project.screenrecorder.Repository.VideoRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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

    @Value("${app.chunk-temp-dir}")
    private String chunkTempDir;

    @Value("${minio.buckets.videos}")
    private String finalVideos;

    @Value("${app.share-base-url}")
    private String baseUrl;

    private Path getChunkTempDir() {
        if (chunkTempDir != null && !chunkTempDir.isBlank()) {
            return Path.of(chunkTempDir);  // use if explicitly set
        }
        // Auto-detect OS temp dir
        return Path.of(System.getProperty("java.io.tmpdir"), "chunks");
    }

    @Value("${app.ffmpeg-path}")
    private String ffmpegPath;



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

            String minioPath = videoId + "/" + chunkIndex;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(chunksBucket)
                            .object(minioPath)
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

    public CompleteUploadResponse completeUpload(String videoId) throws  Exception{






            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("videoId" + videoId + " not found "));



            List<VideoChunk> chunks = videoChunkRepository.findByVideoIdOrderByChunkIndexAsc(videoId);

            if (chunks.isEmpty()){
                throw new RuntimeException("No chunks found for video : " + videoId);
            }
            Path tempDir = getChunkTempDir().resolve(videoId);
            Files.createDirectories(tempDir);


        try {

            //1. step 1 :
            // downloading each chunk from MinIO to temp folder
            List<Path> chunkFilePaths = new ArrayList<>(); // collecting chunks paths for step 2
            for( VideoChunk chunk : chunks){

                Path chunkFilePath = tempDir.resolve(chunk.getChunkIndex() + ".webm"); // work on existing path

                //try with resources block
                try(InputStream stream = minioClient.getObject( // () - resource opened in declaration
                                GetObjectArgs.builder()
                                        .bucket(chunksBucket)
                                        .object(chunk.getMinioPath())
                                        .build())){
                    Files.copy(stream,chunkFilePath, StandardCopyOption.REPLACE_EXISTING);
                } // resource is closed

                chunkFilePaths.add(chunkFilePath);
            }

            // 2. ffmpeg needs a textfile with paths to execute merging

            Path concatFile = tempDir.resolve("filelist.txt");
            try(BufferedWriter writer = Files.newBufferedWriter(concatFile)){
                for (Path chunkFilePath : chunkFilePaths ){
                    writer.write("file '" + chunkFilePath.toAbsolutePath() + "'");
                    writer.newLine();
                }
            }

            // 3. execute ffmpeg -f concat -safe 0 -i filelist.txt -c copy output.mp4 using process builder
            Path outputFile = tempDir.resolve("output.mp4");
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-f", "concat",
                    "-safe", "0",
                    "-i" , concatFile.toAbsolutePath().toString(),
                    "-c","copy",
                    outputFile.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            String ffmpegOutput = new String(process.getInputStream().readAllBytes());
            System.out.println("FFmpeg output: " + ffmpegOutput);

            int exitcode = process.waitFor();

            if(exitcode != 0) {
                video.setStatus(Video.VideoStatus.PROCESSING_FAILED);
                videoRepository.save(video);
                throw new RuntimeException("FFmpeg failed with exit code " + exitcode);
            }



            // 4. Upload merged file to MinIO
            String finalMinioPath  = videoId + "/output.mp4";
            try(InputStream outputStream = Files.newInputStream(outputFile)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(finalVideos)
                                .object(finalMinioPath)
                                .stream(outputStream,Files.size(outputFile),-1)
                                .contentType("video/mp4")
                                .build());
            }

            // 5. Delete chunks from MinIO
            for (VideoChunk chunk : chunks) {

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(chunksBucket)
                                .object(chunk.getMinioPath())
                                .build()
                );
            }

            // 6. Update video in DB

            video.setMinioPath(finalMinioPath);
            video.setStatus(Video.VideoStatus.READY);
            videoRepository.save(video);

            // 7. Build and return response

            CompleteUploadResponse response = new CompleteUploadResponse();
            response.setToken(video.getToken());
            response.setShareUrl(baseUrl + "/watch/" + video.getToken());
            response.setStatus(video.getStatus().name());
            return response;


        } finally {
            // 8.  Cleanup temp folder always, even if exception thrown
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.delete(path); }
                            catch (IOException ignored) {}
                        });

            }
        }

    }
}
