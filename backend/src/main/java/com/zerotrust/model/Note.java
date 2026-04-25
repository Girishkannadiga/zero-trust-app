package com.zerotrust.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Note() {}

    private Note(Builder b) {
        this.user    = b.user;
        this.title   = b.title;
        this.content = b.content;
    }

    public Long getId()                  { return id; }
    public User getUser()                { return user; }
    public String getTitle()             { return title; }
    public String getContent()           { return content; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setUser(User user)               { this.user = user; }
    public void setTitle(String title)           { this.title = title; }
    public void setContent(String content)       { this.content = content; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User user;
        private String title;
        private String content;

        public Builder user(User user)         { this.user = user; return this; }
        public Builder title(String title)     { this.title = title; return this; }
        public Builder content(String content) { this.content = content; return this; }

        public Note build() { return new Note(this); }
    }
}
