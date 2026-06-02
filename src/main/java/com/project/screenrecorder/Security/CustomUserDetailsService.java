package com.project.screenrecorder.Security;


import com.project.screenrecorder.Entity.Video;
import com.project.screenrecorder.Exception.VideoNotFoundException;
import com.project.screenrecorder.Repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final VideoRepository videoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Video video = videoRepository.findByToken(username).
                orElseThrow(()-> new VideoNotFoundException("Video with "+username+ " not found"));

        return new SecurityBridge(video);
    }
}
