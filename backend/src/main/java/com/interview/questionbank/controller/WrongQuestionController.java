package com.interview.questionbank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.questionbank.common.Result;
import com.interview.questionbank.entity.WrongQuestion;
import com.interview.questionbank.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {
    
    private final WrongQuestionService wrongQuestionService;
    
    @GetMapping
    public Result<Page<WrongQuestion>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<WrongQuestion> result = wrongQuestionService.pageWithDetails(page, size);
        return Result.success(result);
    }
    
    @GetMapping("/all")
    public Result<List<WrongQuestion>> getAll() {
        List<WrongQuestion> result = wrongQuestionService.getAllWithDetails();
        return Result.success(result);
    }
    
    @PostMapping
    public Result<WrongQuestion> addManual(@RequestParam Long questionId) {
        WrongQuestion result = wrongQuestionService.addManual(questionId);
        return Result.success(result);
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        boolean result = wrongQuestionService.removeFromWrong(id);
        return Result.success(result);
    }
    
    @DeleteMapping("/question/{questionId}")
    public Result<Boolean> removeByQuestionId(@PathVariable Long questionId) {
        boolean result = wrongQuestionService.removeByQuestionId(questionId);
        return Result.success(result);
    }
}
