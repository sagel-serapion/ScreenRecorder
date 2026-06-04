package com.project.screenrecorder.Security;


import com.project.screenrecorder.Entity.Video;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class SecurityBridge implements UserDetails {

    private final Video video;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return video.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return video.getToken();
    }

    public String getVideoId(){
        return video.getId();
    }
}
