package com.project.screenrecorder.Repository;

import com.project.screenrecorder.Entity.VideoChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VideoChunkRepository extends JpaRepository<VideoChunk,String> {
}
