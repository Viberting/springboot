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

    // 根据文章ID查询评论（用于文章详情页展示评论，保留关联用户表拿头像的逻辑）
    @Select("SELECT " +
            "t_comment.*, " +
            "t_article.title AS articleTitle, " +
            "t_user.avatar AS userAvatar " + // 关联用户表拿头像（保留原有逻辑）
            "FROM t_comment " +
            "LEFT JOIN t_article ON t_comment.article_id = t_article.id " + // 左连接文章表，保证评论不丢
            "LEFT JOIN t_user ON t_comment.user_id = t_user.id " + // 左连接用户表，拿头像（保留）
            "WHERE (#{articleId} IS NULL OR #{articleId} <= 0 OR t_comment.article_id = #{articleId}) " +
            "AND t_comment.is_deleted = 0 " +
            "AND (t_comment.status = 'approved' OR t_comment.status = '1')")
    IPage<CommentVO> getCommentByArticleId(IPage<CommentVO> page, @Param("articleId") Integer articleId);

    // 根据文章ID分页查询评论（用于文章详情页展示评论，保留关联用户表拿头像）
    @Select("SELECT " +
            "t_comment.*, " +
            "t_article.title AS articleTitle, " +
            "t_user.avatar AS userAvatar " + // 直接返回带/api的路径，不替换（保留原有逻辑）
            "FROM t_comment " +
            "LEFT JOIN t_article ON t_comment.article_id = t_article.id " +
            "LEFT JOIN t_user ON t_comment.user_id = t_user.id " +
            "WHERE (#{articleId} IS NULL OR #{articleId} <= 0 OR t_comment.article_id = #{articleId}) " +
            "AND t_comment.is_deleted = 0 " +
            "AND (t_comment.status = 'approved' OR t_comment.status = '1')")
    IPage<CommentVO> getAPageCommentByArticleId(IPage<CommentVO> page, @Param("articleId") Integer articleId);

    // 新增：查询评论的回复（适配回复场景，同时保留关联用户表拿头像）
    @Select("SELECT " +
            "t_comment.*, " +
            "t_article.title AS articleTitle, " +
            "t_user.avatar AS userAvatar " + // 回复评论也展示用户头像（对齐原有逻辑）
            "FROM t_comment " +
            "LEFT JOIN t_article ON t_comment.article_id = t_article.id " + // 左连接保证回复不丢
            "LEFT JOIN t_user ON t_comment.user_id = t_user.id " + // 回复评论关联用户头像
            "WHERE (#{parentId} IS NULL OR #{parentId} <= 0 OR t_comment.parent_id = #{parentId}) " + // 根据父评论ID查回复
            "AND t_comment.is_deleted = 0 " +
            "AND (t_comment.status = 'approved' OR t_comment.status = '1') " +
            "ORDER BY t_comment.created ASC") // 回复按时间升序展示
    IPage<CommentVO> getCommentReplies(IPage<CommentVO> page, @Param("parentId") Integer parentId);

    // 搜索评论的SQL（兼容status的字符串/数字格式，保留关联用户表拿头像）
    @Select("SELECT t_comment.*, t_article.title AS articleTitle, t_user.avatar AS userAvatar " + // 保留userAvatar
            "FROM t_comment " +
            "LEFT JOIN t_article ON t_comment.article_id = t_article.id " + // 保留左连接（核心）
            "LEFT JOIN t_user ON t_comment.user_id = t_user.id " + // 保留左连接user表拿头像
            "WHERE t_comment.is_deleted = 0 " +
            "AND (#{content} IS NULL OR #{content} = '' OR t_comment.content LIKE CONCAT('%', #{content}, '%')) " +
            "AND (#{articleId} IS NULL OR t_comment.article_id = #{articleId}) " +
            "AND (#{author} IS NULL OR #{author} = '' OR t_comment.author LIKE CONCAT('%', #{author}, '%')) " +
            "AND (#{status} IS NULL OR " +
            "    (CASE WHEN #{status} = 1 THEN t_comment.status IN ('approved', '1') " + // 保留原有逻辑
            "          WHEN #{status} = 0 THEN t_comment.status IN ('pending', '0') " + // 保留原有逻辑
            "     END)) " +
            "ORDER BY t_comment.created DESC")
    IPage<CommentVO> searchComments(IPage<CommentVO> page,
                                    @Param("content") String content,
                                    @Param("articleId") Integer articleId,
                                    @Param("author") String author,
                                    @Param("status") Integer status);

    // 搜索总数的SQL（同上述条件，保持原有逻辑不变）
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