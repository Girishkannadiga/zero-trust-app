package com.zerotrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 10000, message = "Content must not exceed 10 000 characters")
    private String content;

    public NoteRequest() {}

    public String getTitle()             { return title; }
    public String getContent()           { return content; }
    public void setTitle(String title)   { this.title = title; }
    public void setContent(String c)     { this.content = c; }
}
