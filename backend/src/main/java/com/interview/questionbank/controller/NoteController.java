package com.interview.questionbank.controller;

import com.interview.questionbank.common.Result;
import com.interview.questionbank.entity.Note;
import com.interview.questionbank.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;
    
    @GetMapping("/question/{questionId}")
    public Result<Note> getByQuestionId(@PathVariable Long questionId) {
        Note note = noteService.getByQuestionId(questionId);
        return Result.success(note);
    }
    
    @PostMapping
    public Result<Note> createOrUpdate(@RequestBody Note note) {
        Note saved = noteService.saveOrUpdateNote(note);
        return Result.success(saved);
    }
    
    @DeleteMapping("/question/{questionId}")
    public Result<Boolean> deleteByQuestionId(@PathVariable Long questionId) {
        boolean result = noteService.deleteByQuestionId(questionId);
        return Result.success(result);
    }
}
