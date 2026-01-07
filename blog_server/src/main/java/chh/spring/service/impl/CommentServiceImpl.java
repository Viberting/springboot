package chh.spring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import chh.spring.entity.Comment;
import chh.spring.entity.vo.CommentVO;
import chh.spring.mapper.CommentMapper;
import chh.spring.service.CommentService;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional // 支持事务
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    // 分页查询所有评论
    @Override
    public Result getCommentPage(PageParams pageParams) {
        Result result = new Result();
        // 构建查询条件：按评论时间降序
        QueryWrapper<CommentVO> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("t_comment.created");

        // 分页对象（页号、每页条数）
        Page<CommentVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
        IPage<CommentVO> commentPage = commentMapper.getCommentPage(page, wrapper);

        // 封装结果（评论列表 + 分页信息）
        pageParams.setTotal(commentPage.getTotal());
        result.getMap().put("commentVOs", commentPage.getRecords());
        result.getMap().put("pageParams", pageParams);
        return result;
    }

    // 根据文章ID查询评论
    @Override
    public Result getCommentByArticleId(Integer articleId, PageParams pageParams) {
        Result result = new Result();
        Page<CommentVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
        IPage<CommentVO> commentPage = commentMapper.getCommentByArticleId(page, articleId);

        pageParams.setTotal(commentPage.getTotal());
        result.getMap().put("commentVOs", commentPage.getRecords());
        result.getMap().put("pageParams", pageParams);
        return result;
    }

    // 新增评论：补充ip、status字段
    @Override
    public void addComment(Comment comment) {
        comment.setCreated(new Date());
        comment.setIsDeleted(0);
        comment.setStatus("approved"); // 默认状态（已审核）
        
        // 如果author为空，则设置默认值
        if (comment.getAuthor() == null || comment.getAuthor().trim().isEmpty()) {
            comment.setAuthor("匿名用户");
        }
        
        commentMapper.insert(comment);
    }

    // 编辑评论：增加ip、status的修改
    @Override
    public void updateComment(Comment comment) {
        Comment newComment = commentMapper.selectById(comment.getId());
        if (newComment != null) {
            newComment.setContent(comment.getContent());
            newComment.setAuthor(comment.getAuthor());
            newComment.setIp(comment.getIp()); // 新增
            newComment.setStatus(comment.getStatus()); // 新增
            commentMapper.updateById(newComment);
        }
    }

    // 逻辑删除评论（修改is_deleted为1）
    @Override
    public void deleteComment(Integer id) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null) {
            comment.setIsDeleted(1);
            commentMapper.updateById(comment);
        }
    }

    // 根据ID查询单条评论
    @Override
    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    // 按条件搜索评论
    @Override
    public Result searchComments(String content, Integer articleId, String author, String status, PageParams pageParams) {
        Result result = new Result();
        try {
            // 检查pageParams是否为null
            if (pageParams == null) {
                pageParams = new PageParams();
                pageParams.setPage(1L);
                pageParams.setRows(10L);
            }

            // 使用MyBatis-Plus分页插件
            Page<CommentVO> page = new Page<>(
                    pageParams.getPage() != null ? pageParams.getPage() : 1L,
                    pageParams.getRows() != null ? pageParams.getRows() : 10L
            );

            // 对参数进行null检查
            String safeContent = (content != null && content.trim().isEmpty()) ? null : content;
            String safeAuthor = (author != null && author.trim().isEmpty()) ? null : author;
            String safeStatus = status;  // 保持String类型
            Integer safeArticleId = articleId;

            // 执行搜索查询（使用分页插件）
            IPage<CommentVO> commentPage = commentMapper.searchComments(
                    page, safeContent, safeArticleId, safeAuthor, safeStatus != null && !"null".equals(safeStatus) ? Integer.valueOf(safeStatus) : null
            );

            // 设置总数
            pageParams.setTotal(commentPage.getTotal());

            // 封装结果
            result.getMap().put("commentVOs", commentPage.getRecords());
            result.getMap().put("pageParams", pageParams);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setErrorMessage("搜索评论失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}