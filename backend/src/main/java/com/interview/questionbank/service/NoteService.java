package com.interview.questionbank.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@Service
@RequiredArgsConstructor
public class NoteService extends ServiceImpl<NoteMapper, Note> {
    
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    
    public Note getByQuestionId(Long questionId) {
        Note note = this.getOne(new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, questionId));
        if (note != null) {
            Question question = questionMapper.selectById(questionId);
            note.setQuestion(question);
        }
        return note;
    }
    
    @Transactional
    public Note saveOrUpdateNote(Note note) {
        Note existing = this.getOne(new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, note.getQuestionId()));
        
        if (existing != null) {
            note.setId(existing.getId());
            this.updateById(note);
        } else {
            this.save(note);
        }
        
        updateWrongQuestion(note);
        
        return this.getById(note.getId());
    }
    
    private void updateWrongQuestion(Note note) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<WrongQuestion>()
            .eq(WrongQuestion::getQuestionId, note.getQuestionId());
        
        if (note.getMasteryLevel() == 0) {
            WrongQuestion wrong = wrongQuestionMapper.selectOne(wrapper);
            if (wrong == null) {
                wrong = new WrongQuestion();
                wrong.setQuestionId(note.getQuestionId());
                wrong.setNoteId(note.getId());
                wrongQuestionMapper.insert(wrong);
            }
        } else {
            wrongQuestionMapper.delete(wrapper);
        }
    }
    
    @Transactional
    public boolean deleteByQuestionId(Long questionId) {
        wrongQuestionMapper.delete(new LambdaQueryWrapper<WrongQuestion>()
            .eq(WrongQuestion::getQuestionId, questionId));
        return this.remove(new LambdaQueryWrapper<Note>().eq(Note::getQuestionId, questionId));
    }
}
