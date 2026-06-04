package com.project.screenrecorder.Controller;


import com.project.screenrecorder.DTO.VideoAuthRequest;
import com.project.screenrecorder.Security.SecurityBridge;
import com.project.screenrecorder.Service.WatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/watch")
@RequiredArgsConstructor
public class WatchController {

    private final WatchService watchService;


    @GetMapping("/{token}")
    ResponseEntity<String> watchUrl(
            @PathVariable String token
    ){
        String url = watchService.getWatchUrl(token );
        return ResponseEntity.ok(url);
    }

    @PostMapping("/{token}/auth")
    ResponseEntity<String> authenticate(
            @PathVariable String token,
            VideoAuthRequest videoAuthRequest
            ){
        String jwt  = watchService.authenticateVideo(token , videoAuthRequest.getPassword());
        return ResponseEntity.ok(jwt);
    }

    @GetMapping("/{token}/stream")
    ResponseEntity<String> watchUrlStream(
            @AuthenticationPrincipal SecurityBridge current ,
            @PathVariable String token
    ){
        String url = watchService.getWatchUrl(token , current);
        return ResponseEntity.ok(url);
    }


}
