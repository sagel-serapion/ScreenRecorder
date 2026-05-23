package com.project.screenrecorder.Repository;

import com.project.screenrecorder.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VideoRepository extends JpaRepository<Video,String> {
}
