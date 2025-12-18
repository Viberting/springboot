package chh.spring.mapper;

import chh.spring.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {
    //当只有一个参数时，传进去一个值也只有一个参数可以匹配，这时参数名可以省略
    @Select("SELECT * FROM t_comment WHERE article_id=#{articleId} ORDER BY id DESC")
    public List< Comment> selectByArticleId(Integer articleId);

    //多个参数时，可以使用@Param注解
    //分页查询一条新闻的一页评论
    @Select("SELECT * FROM t_comment WHERE article_id=#{articleId} ORDER BY id DESC limit #{offset},#{size}")
    public List< Comment> getAPageCommentByArticleId(
            @Param("articleId") Integer articleId,
            @Param("offset") Integer offset,    //offset表示从符合条件的第几条记录开始返回，从0开始计算
            @Param("size") Integer size);   //size表示返回几条记录
}
