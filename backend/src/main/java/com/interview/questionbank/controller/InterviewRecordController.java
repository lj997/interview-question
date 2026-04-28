package com.interview.questionbank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.questionbank.common.Result;
import com.interview.questionbank.entity.InterviewRecord;
import com.interview.questionbank.service.InterviewRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/interview-records")
@RequiredArgsConstructor
public class InterviewRecordController {
    
    private final InterviewRecordService interviewRecordService;
    
    @GetMapping
    public Result<Page<InterviewRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String company) {
        Page<InterviewRecord> result = interviewRecordService.pageWithQuestions(page, size, company);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result<InterviewRecord> getById(@PathVariable Long id) {
        InterviewRecord record = interviewRecordService.getByIdWithQuestions(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        return Result.success(record);
    }
    
    @PostMapping
    public Result<InterviewRecord> create(@RequestBody CreateInterviewRequest request) {
        InterviewRecord record = new InterviewRecord();
        record.setCompany(request.getCompany());
        record.setInterviewDate(request.getInterviewDate());
        record.setSummary(request.getSummary());
        
        InterviewRecord saved = interviewRecordService.createRecord(record, request.getQuestionIds());
        return Result.success(saved);
    }
    
    @PutMapping("/{id}")
    public Result<InterviewRecord> update(@PathVariable Long id, @RequestBody CreateInterviewRequest request) {
        InterviewRecord record = new InterviewRecord();
        record.setId(id);
        record.setCompany(request.getCompany());
        record.setInterviewDate(request.getInterviewDate());
        record.setSummary(request.getSummary());
        
        InterviewRecord updated = interviewRecordService.updateRecord(record, request.getQuestionIds());
        return Result.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = interviewRecordService.removeById(id);
        return Result.success(result);
    }
    
    @lombok.Data
    public static class CreateInterviewRequest {
        private String company;
        private java.time.LocalDate interviewDate;
        private List<Long> questionIds;
        private String summary;
    }
}
