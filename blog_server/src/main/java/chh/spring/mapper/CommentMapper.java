package chh.spring.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import chh.spring.entity.Comment;
import chh.spring.entity.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    // 多表分页查询：评论表 + 文章表（获取文章标题）
    @Select("SELECT t_comment.*, t_article.title AS articleTitle " +
            "FROM t_comment, t_article " +
            "WHERE t_comment.article_id = t_article.id " +
            "AND t_comment.is_deleted = 0 " + // 只查未删除的评论
            "${ew.customSqlSegment}") // 动态条件（排序、筛选）
    IPage<CommentVO> getCommentPage(IPage<CommentVO> page, @Param("ew") Wrapper<CommentVO> wrapper);

    // 根据文章ID查询评论（用于文章详情页展示评论）
    @Select("SELECT t_comment.*, t_article.title AS articleTitle " +
            "FROM t_comment, t_article " +
            "WHERE t_comment.article_id = t_article.id " +
            "AND t_comment.article_id = #{articleId} " +
            "AND t_comment.is_deleted = 0 " +
            "AND (t_comment.status = 'approved' OR t_comment.status = '1')")
    IPage<CommentVO> getCommentByArticleId(IPage<CommentVO> page, @Param("articleId") Integer articleId);
    
    // 根据文章ID分页查询评论（用于文章详情页展示评论）
    @Select("SELECT t_comment.*, t_article.title AS articleTitle " +
            "FROM t_comment, t_article " +
            "WHERE t_comment.article_id = t_article.id " +
            "AND t_comment.article_id = #{articleId} " +
            "AND t_comment.is_deleted = 0 " +
            "AND (t_comment.status = 'approved' OR t_comment.status = '1') " +
            "LIMIT #{offset}, #{limit}")
    IPage<CommentVO> getAPageCommentByArticleId(@Param("articleId") Integer articleId, @Param("offset") int offset, @Param("limit") int limit);
}