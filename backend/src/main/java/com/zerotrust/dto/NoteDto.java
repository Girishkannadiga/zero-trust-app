package com.zerotrust.dto;

import java.time.LocalDateTime;

public class NoteDto {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoteDto() {}

    private NoteDto(Builder b) {
        this.id        = b.id;
        this.title     = b.title;
        this.content   = b.content;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public Long getId()                  { return id; }
    public String getTitle()             { return title; }
    public String getContent()           { return content; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setId(Long id)                          { this.id = id; }
    public void setTitle(String title)                  { this.title = title; }
    public void setContent(String content)              { this.content = content; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                          { this.id = id; return this; }
        public Builder title(String title)                  { this.title = title; return this; }
        public Builder content(String content)              { this.content = content; return this; }
        public Builder createdAt(LocalDateTime createdAt)   { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; return this; }

        public NoteDto build() { return new NoteDto(this); }
    }
}
