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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    // 根据文章ID查询评论（保留原有关联用户表查头像的逻辑）
    @Override
    public Result getCommentByArticleId(Integer articleId, PageParams pageParams) {
        Result result = new Result();
        Page<CommentVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
        // 关键保留：调用关联用户表的 getAPageCommentByArticleId 方法，保证返回userAvatar
        IPage<CommentVO> commentPage = commentMapper.getAPageCommentByArticleId(page, articleId != null ? articleId : 0);

        pageParams.setTotal(commentPage.getTotal());
        result.getMap().put("commentVOs", commentPage.getRecords());
        result.getMap().put("pageParams", pageParams);
        return result;
    }

    // 新增：根据父评论ID查询回复（适配回复场景，保留用户头像返回）
    @Override
    public Result getCommentReplies(Integer parentId, PageParams pageParams) {
        Result result = new Result();
        Page<CommentVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
        IPage<CommentVO> commentPage = commentMapper.getCommentReplies(page, parentId != null ? parentId : 0);

        pageParams.setTotal(commentPage.getTotal());
        result.getMap().put("commentVOs", commentPage.getRecords());
        result.getMap().put("pageParams", pageParams);
        return result;
    }

    // 新增：获取文章的完整评论树（包含多级回复，保留用户头像）
    @Override
    public Result getCommentTreeByArticleId(Integer articleId) {
        Result result = new Result();
        try {
            // 1. 先分页查询该文章的所有评论（复用原有关联用户表的方法，保证能拿到userAvatar）
            // 这里不分页，所以构造一个超大页容量的分页对象
            Page<CommentVO> page = new Page<>(1, Integer.MAX_VALUE);
            IPage<CommentVO> commentPage = commentMapper.getAPageCommentByArticleId(page, articleId != null ? articleId : 0);
            List<CommentVO> allCommentVOs = commentPage.getRecords();

            // 2. 构建评论树
            List<CommentVO> topLevelComments = new ArrayList<>();
            for (CommentVO commentVO : allCommentVOs) {
                // 查找顶级评论（parentId为null或0）
                if (commentVO.getParentId() == null || commentVO.getParentId() <= 0) {
                    commentVO.setChildren(getChildrenComments(allCommentVOs, commentVO.getId()));
                    topLevelComments.add(commentVO);
                }
            }

            // 3. 封装结果
            result.getMap().put("comments", topLevelComments);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setErrorMessage("获取评论树失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 私有方法：从所有评论VO中查找指定评论ID的直接子评论（递归构建树形）
    private List<CommentVO> getChildrenComments(List<CommentVO> allCommentVOs, Integer parentId) {
        List<CommentVO> children = new ArrayList<>();
        for (CommentVO commentVO : allCommentVOs) {
            if (commentVO.getParentId() != null && commentVO.getParentId().equals(parentId)) {
                // 递归查找子评论的子评论
                commentVO.setChildren(getChildrenComments(allCommentVOs, commentVO.getId()));
                children.add(commentVO);
            }
        }
        return children;
    }

    // 新增评论：保留原有逻辑，补充回复相关字段处理
    @Override
    public void addComment(Comment comment) {
        comment.setCreated(new Date());
        comment.setIsDeleted(0);
        comment.setStatus("approved"); // 默认状态（已审核）

        // 原有逻辑：处理匿名作者
        if (comment.getAuthor() == null || comment.getAuthor().trim().isEmpty()) {
            comment.setAuthor("匿名用户");
        }

        // 新增逻辑：处理评论回复的rootId和parentId
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            // 这是回复评论，设置根评论ID
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                // 父评论有根ID则继承，无则用父评论ID作为根ID
                comment.setRootId(parentComment.getRootId() != null ? parentComment.getRootId() : parentComment.getId());
            }
        } else {
            // 顶级评论，先设临时值，插入后更新为自身ID
            comment.setRootId(0);
        }

        // 插入评论
        commentMapper.insert(comment);

        // 顶级评论更新rootId为自身ID
        if (comment.getParentId() == null || comment.getParentId() <= 0) {
            comment.setRootId(comment.getId());
            commentMapper.updateById(comment);
        }
    }

    // 编辑评论：保留原有逻辑，补充回复相关字段修改
    @Override
    public void updateComment(Comment comment) {
        if (comment == null || comment.getId() == null || comment.getId() <= 0) {
            return;
        }
        Comment newComment = commentMapper.selectById(comment.getId());
        if (newComment != null) {
            // 原有字段修改
            newComment.setContent(comment.getContent());
            newComment.setAuthor(comment.getAuthor());
            newComment.setIp(comment.getIp());
            newComment.setStatus(comment.getStatus());

            // 新增：回复相关字段修改
            newComment.setParentId(comment.getParentId());
            newComment.setRootId(comment.getRootId());
            newComment.setReplyTo(comment.getReplyTo());

            commentMapper.updateById(newComment);
        }
    }

    // 逻辑删除评论（原有逻辑不变）
    @Override
    public void deleteComment(Integer id) {
        if (id != null && id > 0) {
            Comment comment = commentMapper.selectById(id);
            if (comment != null) {
                comment.setIsDeleted(1);
                commentMapper.updateById(comment);
            }
        }
    }

    // 根据ID查询单条评论（原有逻辑不变）
    @Override
    public Comment selectById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return commentMapper.selectById(id);
    }

    // 按条件搜索评论（原有逻辑完全保留）
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
            String safeContent = (content != null && !content.trim().isEmpty()) ? content : null;
            String safeAuthor = (author != null && !author.trim().isEmpty()) ? author : null;
            String safeStatus = status;  // 保持String类型
            Integer safeArticleId = articleId;

            // 执行搜索查询（使用分页插件），对status参数进行安全转换
            Integer statusInt = null;
            if (safeStatus != null && !safeStatus.isEmpty() && !"null".equals(safeStatus) && !"undefined".equals(safeStatus)) {
                try {
                    statusInt = Integer.valueOf(safeStatus);
                } catch (NumberFormatException e) {
                    // 如果状态不是有效的数字，保持为null
                    statusInt = null;
                }
            }

            // 执行搜索查询（使用分页插件）
            IPage<CommentVO> commentPage = commentMapper.searchComments(
                    page, safeContent, safeArticleId, safeAuthor, statusInt
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