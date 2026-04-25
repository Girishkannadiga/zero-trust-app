package com.zerotrust.service;

import com.zerotrust.dto.NoteDto;
import com.zerotrust.dto.NoteRequest;
import com.zerotrust.exception.UnauthorizedAccessException;
import com.zerotrust.model.Note;
import com.zerotrust.model.User;
import com.zerotrust.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<NoteDto> getNotes(User user) {
        return noteRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public NoteDto createNote(User user, NoteRequest request) {
        Note note = Note.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        return toDto(noteRepository.save(note));
    }

    public NoteDto updateNote(User user, Long id, NoteRequest request) {
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new UnauthorizedAccessException("Note not found or access denied."));
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        return toDto(noteRepository.save(note));
    }

    public void deleteNote(User user, Long id) {
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new UnauthorizedAccessException("Note not found or access denied."));
        noteRepository.delete(note);
    }

    private NoteDto toDto(Note note) {
        return NoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
