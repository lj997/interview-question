package com.interview.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("interview_records")
public class InterviewRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String company;
    
    private LocalDate interviewDate;
    
    private String questionIds;
    
    private String summary;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private List<Question> questions;
}
