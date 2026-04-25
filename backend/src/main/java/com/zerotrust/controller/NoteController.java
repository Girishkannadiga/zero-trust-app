package com.zerotrust.controller;

import com.zerotrust.dto.ApiResponse;
import com.zerotrust.dto.NoteDto;
import com.zerotrust.dto.NoteRequest;
import com.zerotrust.service.NoteService;
import com.zerotrust.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteDto>>> getNotes(
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(noteService.getNotes(user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteDto>> createNote(
            @Valid @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Note created.", noteService.createNote(user, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteDto>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Note updated.", noteService.updateNote(user, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        noteService.deleteNote(user, id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted.", null));
    }
}
