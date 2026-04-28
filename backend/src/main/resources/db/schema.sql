CREATE DATABASE IF NOT EXISTS interview_bank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE interview_bank;

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL COMMENT '题目内容',
    answer_points TEXT COMMENT '答案要点',
    category VARCHAR(100) NOT NULL COMMENT '分类：Java/算法/数据库等',
    difficulty TINYINT NOT NULL DEFAULT 2 COMMENT '难度：1简单/2中等/3困难',
    company VARCHAR(100) COMMENT '来源公司',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_company (company),
    FULLTEXT idx_content (content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

CREATE TABLE IF NOT EXISTS notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL COMMENT '题目ID',
    my_answer TEXT COMMENT '我的回答思路',
    mastery_level TINYINT NOT NULL DEFAULT 0 COMMENT '掌握程度：0未掌握/1了解/2掌握/3熟练',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_question_id (question_id),
    INDEX idx_mastery_level (mastery_level),
    CONSTRAINT fk_notes_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人笔记表';

CREATE TABLE IF NOT EXISTS wrong_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL COMMENT '题目ID',
    note_id BIGINT COMMENT '笔记ID',
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_question_id (question_id),
    INDEX idx_added_at (added_at),
    CONSTRAINT fk_wrong_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_wrong_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

CREATE TABLE IF NOT EXISTS interview_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company VARCHAR(100) NOT NULL COMMENT '面试公司',
    interview_date DATE NOT NULL COMMENT '面试日期',
    question_ids TEXT NOT NULL COMMENT '题目ID列表，JSON数组格式',
    summary TEXT COMMENT '经验总结',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company (company),
    INDEX idx_interview_date (interview_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';
