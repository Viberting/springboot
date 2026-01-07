package chh.spring.service;

import chh.spring.entity.Comment;
import chh.spring.entity.vo.CommentVO;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;

public interface CommentService {
    // 分页查询所有评论（多表关联）
    Result getCommentPage(PageParams pageParams);

    // 根据文章ID查询评论
    Result getCommentByArticleId(Integer articleId, PageParams pageParams);

    // 新增评论
    void addComment(Comment comment);

    // 编辑评论
    void updateComment(Comment comment);

    // 删除评论（逻辑删除）
    void deleteComment(Integer id);

    // 根据ID查询单条评论（用于编辑回显）
    Comment selectById(Integer id);

    // 按条件搜索评论
    Result searchComments(String content, Integer articleId, String author, String status, PageParams pageParams);
}