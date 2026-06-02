package com.project.screenrecorder.Exception;

public class VideoNotReadyException extends RuntimeException{

    public VideoNotReadyException(String message){
        super(message);
    }
}
