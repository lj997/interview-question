package com.interview.questionbank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.questionbank.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    @Select("<script>" +
            "SELECT * FROM questions WHERE 1=1 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "<if test='difficulty != null'> AND difficulty = #{difficulty} </if>" +
            "<if test='company != null and company != \"\"'> AND company = #{company} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND MATCH(content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) </if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    List<Question> searchQuestions(@Param("category") String category,
                                    @Param("difficulty") Integer difficulty,
                                    @Param("company") String company,
                                    @Param("keyword") String keyword);
    
    @Select("<script>" +
            "SELECT * FROM questions WHERE 1=1 " +
            "<if test='category != null and category != \"\"'> AND category = #{category} </if>" +
            "<if test='difficulty != null'> AND difficulty = #{difficulty} </if>" +
            " ORDER BY RAND() LIMIT #{limit}" +
            "</script>")
    List<Question> selectRandom(@Param("category") String category,
                                 @Param("difficulty") Integer difficulty,
                                 @Param("limit") int limit);
    
    @Select("SELECT DISTINCT category FROM questions ORDER BY category")
    List<String> selectAllCategories();
    
    @Select("SELECT DISTINCT company FROM questions WHERE company IS NOT NULL AND company != '' ORDER BY company")
    List<String> selectAllCompanies();
    
    @Select("<script>" +
            "SELECT * FROM questions WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach>" +
            "</script>")
    List<Question> selectByIds(@Param("ids") List<Long> ids);
}
