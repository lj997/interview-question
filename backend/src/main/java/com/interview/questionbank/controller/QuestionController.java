package com.interview.questionbank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.questionbank.common.Result;
import com.interview.questionbank.entity.Question;
import com.interview.questionbank.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    
    @GetMapping
    public Result<Page<Question>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String keyword) {
        Page<Question> result = questionService.pageWithNotes(page, size, category, difficulty, company, keyword);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        Question question = questionService.getByIdWithNote(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        return Result.success(question);
    }
    
    @PostMapping
    public Result<Question> create(@RequestBody Question question) {
        questionService.save(question);
        return Result.success(question);
    }
    
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateById(question);
        return Result.success(question);
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = questionService.removeQuestion(id);
        return Result.success(result);
    }
    
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<String> categories = questionService.getAllCategories();
        return Result.success(categories);
    }
    
    @GetMapping("/companies")
    public Result<List<String>> getCompanies() {
        List<String> companies = questionService.getAllCompanies();
        return Result.success(companies);
    }
    
    @GetMapping("/random")
    public Result<List<Question>> randomSelect(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "10") int count) {
        List<Question> questions = questionService.randomSelect(category, difficulty, count);
        return Result.success(questions);
    }
    
    @PostMapping("/import/json")
    public Result<Map<String, Object>> importJson(@RequestBody String jsonContent) {
        try {
            int count = questionService.importFromJson(jsonContent);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("message", "成功导入 " + count + " 道题目");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/import/csv")
    public Result<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            int count = questionService.importFromCsv(file.getBytes());
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("message", "成功导入 " + count + " 道题目");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}
