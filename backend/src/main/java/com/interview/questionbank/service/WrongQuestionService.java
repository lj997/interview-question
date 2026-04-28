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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongQuestionService extends ServiceImpl<WrongQuestionMapper, WrongQuestion> {
    
    private final QuestionMapper questionMapper;
    private final NoteMapper noteMapper;
    
    public Page<WrongQuestion> pageWithDetails(int page, int size) {
        Page<WrongQuestion> wrongPage = new Page<>(page, size);
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WrongQuestion::getAddedAt);
        
        Page<WrongQuestion> result = this.page(wrongPage, wrapper);
        
        for (WrongQuestion wq : result.getRecords()) {
            Question question = questionMapper.selectById(wq.getQuestionId());
            wq.setQuestion(question);
            
            if (wq.getNoteId() != null) {
                Note note = noteMapper.selectById(wq.getNoteId());
                wq.setNote(note);
            }
        }
        
        return result;
    }
    
    public List<WrongQuestion> getAllWithDetails() {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WrongQuestion::getAddedAt);
        
        List<WrongQuestion> list = this.list(wrapper);
        
        for (WrongQuestion wq : list) {
            Question question = questionMapper.selectById(wq.getQuestionId());
            wq.setQuestion(question);
            
            if (wq.getNoteId() != null) {
                Note note = noteMapper.selectById(wq.getNoteId());
                wq.setNote(note);
            }
        }
        
        return list;
    }
    
    @Transactional
    public WrongQuestion addManual(Long questionId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<WrongQuestion>()
            .eq(WrongQuestion::getQuestionId, questionId);
        
        WrongQuestion existing = this.getOne(wrapper);
        if (existing != null) {
            return existing;
        }
        
        Note note = noteMapper.selectOne(
            new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, questionId)
        );
        
        WrongQuestion wrong = new WrongQuestion();
        wrong.setQuestionId(questionId);
        if (note != null) {
            wrong.setNoteId(note.getId());
        }
        this.save(wrong);
        return wrong;
    }
    
    @Transactional
    public boolean removeFromWrong(Long id) {
        return this.removeById(id);
    }
    
    @Transactional
    public boolean removeByQuestionId(Long questionId) {
        return this.remove(new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getQuestionId, questionId));
    }
}
