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
            "AND (t_comment.status = 'approved' OR t_comment.status = '1') ")
    IPage<CommentVO> getAPageCommentByArticleId(IPage<CommentVO> page, @Param("articleId") Integer articleId);

    // 搜索评论的SQL（兼容status的字符串/数字格式）
    @Select("SELECT t_comment.*, t_article.title AS articleTitle " +
            "FROM t_comment, t_article " +
            "WHERE t_comment.article_id = t_article.id " +
            "AND t_comment.is_deleted = 0 " +
            "AND (#{content} IS NULL OR #{content} = '' OR t_comment.content LIKE CONCAT('%', #{content}, '%')) " +
            "AND (#{articleId} IS NULL OR t_comment.article_id = #{articleId}) " +
            "AND (#{author} IS NULL OR #{author} = '' OR t_comment.author LIKE CONCAT('%', #{author}, '%')) " +
            "AND (#{status} IS NULL OR " +
            "    (CASE WHEN #{status} = 1 THEN t_comment.status IN ('approved', '1') " + // 匹配“已审核”的两种格式
            "          WHEN #{status} = 0 THEN t_comment.status IN ('pending', '0') " + // 假设“待审核”是pending/0
            "     END)) " +
            "ORDER BY t_comment.created DESC")
    IPage<CommentVO> searchComments(IPage<CommentVO> page,
                                    @Param("content") String content,
                                    @Param("articleId") Integer articleId,
                                    @Param("author") String author,
                                    @Param("status") Integer status);

    // 搜索总数的SQL（同上述条件）
    @Select("SELECT COUNT(*) " +
            "FROM t_comment, t_article " +
            "WHERE t_comment.article_id = t_article.id " +
            "AND t_comment.is_deleted = 0 " +
            "AND (#{content} IS NULL OR #{content} = '' OR t_comment.content LIKE CONCAT('%', #{content}, '%')) " +
            "AND (#{articleId} IS NULL OR t_comment.article_id = #{articleId}) " +
            "AND (#{author} IS NULL OR #{author} = '' OR t_comment.author LIKE CONCAT('%', #{author}, '%')) " +
            "AND (#{status} IS NULL OR " +
            "    (CASE WHEN #{status} = 1 THEN t_comment.status IN ('approved', '1') " +
            "          WHEN #{status} = 0 THEN t_comment.status IN ('pending', '0') " +
            "     END))")
    int searchCommentsCount(@Param("content") String content,
                            @Param("articleId") Integer articleId,
                            @Param("author") String author,
                            @Param("status") Integer status);
}