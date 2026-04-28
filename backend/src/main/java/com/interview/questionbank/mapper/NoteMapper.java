package com.interview.questionbank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.questionbank.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
