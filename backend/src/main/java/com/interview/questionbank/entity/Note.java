package com.interview.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notes")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long questionId;
    
    private String myAnswer;
    
    private Integer masteryLevel;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private Question question;
}
