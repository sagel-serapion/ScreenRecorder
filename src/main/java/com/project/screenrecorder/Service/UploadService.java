package com.project.screenrecorder.Service;


import com.project.screenrecorder.DTO.ChunkUploadResponse;
import com.project.screenrecorder.DTO.CompleteUploadResponse;
import com.project.screenrecorder.DTO.UploadInitRequest;
import com.project.screenrecorder.DTO.UploadInitResponse;
import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Entity.VideoChunk;
import com.project.screenrecorder.Mapper.VideoMapper;
import com.project.screenrecorder.Repository.VideoChunkRepository;
import com.project.screenrecorder.Repository.VideoRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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

    @Value("${app.chunk-temp-dir}")
    private String chunkTempDir;

    private Path getChunkTempDir() {
        if (chunkTempDir != null && !chunkTempDir.isBlank()) {
            return Path.of(chunkTempDir);  // use if explicitly set
        }
        // Auto-detect OS temp dir
        return Path.of(System.getProperty("java.io.tmpdir"), "chunks");
    }

    @Value("${app.ffmpeg-path.ffmpeg}")
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

    public CompleteUploadResponse completeUpload(String videoId) throws  Exception{

            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("videoId" + videoId + " not found "));



            List<VideoChunk> chunks = videoChunkRepository.findByVideoIdOrderByChunkIndexAsc(videoId);

            if (chunks.isEmpty()){
                throw new RuntimeException("chunks are empty");
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
                    writer.write("file '" + chunkFilePath.toAbsolutePath().toString() + "'");
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
                    "-c","copy", outputFile.toAbsolutePath().toString()

            );




















        } finally {

        }



        CompleteUploadResponse response = new CompleteUploadResponse();
        return response;

    }



}
