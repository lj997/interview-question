package com.interview.questionbank.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.questionbank.entity.Note;
import com.interview.questionbank.entity.Question;
import com.interview.questionbank.entity.WrongQuestion;
import com.interview.questionbank.mapper.NoteMapper;
import com.interview.questionbank.mapper.QuestionMapper;
import com.interview.questionbank.mapper.WrongQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {
    
    private final NoteMapper noteMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final ObjectMapper objectMapper;
    
    public Page<Question> pageWithNotes(int page, int size, String category, Integer difficulty, String company, String keyword) {
        Page<Question> questionPage = new Page<>(page, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Question::getCategory, category);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (company != null && !company.isEmpty()) {
            wrapper.eq(Question::getCompany, company);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Question::getContent, keyword);
        }
        wrapper.orderByDesc(Question::getCreatedAt);
        
        Page<Question> result = this.page(questionPage, wrapper);
        
        for (Question q : result.getRecords()) {
            Note note = noteMapper.selectOne(
                new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, q.getId())
            );
            q.setNote(note);
        }
        
        return result;
    }
    
    public Question getByIdWithNote(Long id) {
        Question question = this.getById(id);
        if (question != null) {
            Note note = noteMapper.selectOne(
                new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, id)
            );
            question.setNote(note);
        }
        return question;
    }
    
    public List<String> getAllCategories() {
        return baseMapper.selectAllCategories();
    }
    
    public List<String> getAllCompanies() {
        return baseMapper.selectAllCompanies();
    }
    
    public List<Question> randomSelect(String category, Integer difficulty, int count) {
        List<Question> questions = baseMapper.selectRandom(category, difficulty, count);
        for (Question q : questions) {
            Note note = noteMapper.selectOne(
                new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, q.getId())
            );
            q.setNote(note);
        }
        return questions;
    }
    
    @Transactional
    public int importFromJson(String jsonContent) throws Exception {
        List<Question> questions = objectMapper.readValue(jsonContent, new TypeReference<List<Question>>() {});
        for (Question q : questions) {
            this.save(q);
        }
        return questions.size();
    }
    
    @Transactional
    public int importFromCsv(byte[] csvBytes) throws Exception {
        String content = new String(csvBytes, StandardCharsets.UTF_8);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        
        CSVFormat format = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim();
        
        int count = 0;
        try (CSVParser parser = new CSVParser(new StringReader(content), format)) {
            for (CSVRecord record : parser) {
                Question q = new Question();
                q.setContent(record.get("content"));
                q.setAnswerPoints(record.get("answer_points") != null ? record.get("answer_points") : "");
                q.setCategory(record.get("category"));
                
                String difficultyStr = record.get("difficulty");
                q.setDifficulty(difficultyStr != null ? Integer.parseInt(difficultyStr) : 2);
                
                q.setCompany(record.get("company") != null ? record.get("company") : "");
                
                this.save(q);
                count++;
            }
        }
        return count;
    }
    
    @Transactional
    public boolean removeQuestion(Long id) {
        wrongQuestionMapper.delete(new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getQuestionId, id));
        noteMapper.delete(new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, id));
        return this.removeById(id);
    }
}
