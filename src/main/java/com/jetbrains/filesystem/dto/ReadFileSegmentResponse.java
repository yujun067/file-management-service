package com.jetbrains.filesystem.dto;

public class ReadFileSegmentResponse {
    //base64 string
    private String data;

    public ReadFileSegmentResponse() {
    }

    public ReadFileSegmentResponse(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
