package com.interview.questionbank.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.questionbank.entity.InterviewRecord;
import com.interview.questionbank.entity.Question;
import com.interview.questionbank.mapper.InterviewRecordMapper;
import com.interview.questionbank.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewRecordService extends ServiceImpl<InterviewRecordMapper, InterviewRecord> {
    
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;
    
    public Page<InterviewRecord> pageWithQuestions(int page, int size, String company) {
        Page<InterviewRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (company != null && !company.isEmpty()) {
            wrapper.like(InterviewRecord::getCompany, company);
        }
        wrapper.orderByDesc(InterviewRecord::getInterviewDate);
        
        Page<InterviewRecord> result = this.page(recordPage, wrapper);
        
        for (InterviewRecord record : result.getRecords()) {
            loadQuestions(record);
        }
        
        return result;
    }
    
    public InterviewRecord getByIdWithQuestions(Long id) {
        InterviewRecord record = this.getById(id);
        if (record != null) {
            loadQuestions(record);
        }
        return record;
    }
    
    @SneakyThrows
    private void loadQuestions(InterviewRecord record) {
        if (record.getQuestionIds() != null && !record.getQuestionIds().isEmpty()) {
            List<Long> ids = objectMapper.readValue(record.getQuestionIds(), new TypeReference<List<Long>>() {});
            if (!ids.isEmpty()) {
                List<Question> questions = questionMapper.selectByIds(ids);
                record.setQuestions(questions);
            }
        }
    }
    
    @SneakyThrows
    public InterviewRecord createRecord(InterviewRecord record, List<Long> questionIds) {
        if (questionIds != null && !questionIds.isEmpty()) {
            record.setQuestionIds(objectMapper.writeValueAsString(questionIds));
        }
        this.save(record);
        return record;
    }
    
    @SneakyThrows
    public InterviewRecord updateRecord(InterviewRecord record, List<Long> questionIds) {
        if (questionIds != null) {
            record.setQuestionIds(objectMapper.writeValueAsString(questionIds));
        }
        this.updateById(record);
        return this.getByIdWithQuestions(record.getId());
    }
}
