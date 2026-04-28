package com.interview.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wrong_questions")
public class WrongQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long questionId;
    
    private Long noteId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addedAt;
    
    @TableField(exist = false)
    private Question question;
    
    @TableField(exist = false)
    private Note note;
}
