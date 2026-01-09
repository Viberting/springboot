package chh.spring.controller;

import chh.spring.entity.Comment;
import chh.spring.service.CommentService;
import chh.spring.tools.CommentSearch;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/comment") // 接口前缀
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 分页查询所有评论
    @PostMapping("/getCommentPage")
    public Result getCommentPage(@RequestBody PageParams pageParams) {
        try {
            return commentService.getCommentPage(pageParams);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询评论失败！");
            e.printStackTrace();
            return result;
        }
    }

    // 根据文章ID分页查询评论
    @PostMapping("/getAPageCommentByArticleId")
    public Result getAPageCommentByArticleId(@RequestParam(required = false) Integer articleId,
                                             @RequestBody PageParams pageParams) {
        try {
            return commentService.getCommentByArticleId(articleId, pageParams);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询文章评论失败！");
            e.printStackTrace();
            return result;
        }
    }

    // 根据文章ID分页查询评论 - GET方式（兼容前端不同请求方式）
    @GetMapping("/getAPageCommentByArticleId")
    public Result getAPageCommentByArticleIdWithGet(@RequestParam(required = false) Integer articleId,
                                                    @RequestParam(defaultValue = "1") Long page,
                                                    @RequestParam(defaultValue = "10") Long rows) {
        try {
            PageParams pageParams = new PageParams();
            pageParams.setPage(page);
            pageParams.setRows(rows);
            return commentService.getCommentByArticleId(articleId, pageParams);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询文章评论失败！");
            e.printStackTrace();
            return result;
        }
    }

    // 新增：根据父评论ID查询回复
    @PostMapping("/getCommentReplies")
    public Result getCommentReplies(@RequestParam(required = false) Integer parentId,
                                    @RequestBody PageParams pageParams) {
        try {
            return commentService.getCommentReplies(parentId, pageParams);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询评论回复失败！");
            e.printStackTrace();
            return result;
        }
    }

    // 新增：获取文章的完整评论树 - 使用@RequestParam方式匹配前端请求
    @GetMapping("/getCommentTreeByArticleId")
    public Result getCommentTreeByArticleId(@RequestParam(required = false) Integer articleId) {
        try {
            if (articleId == null || articleId <= 0) {
                Result result = new Result();
                result.setErrorMessage("文章ID不能为空");
                return result;
            }
            return commentService.getCommentTreeByArticleId(articleId);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询评论树失败！" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 新增/编辑评论（type区分：add/edit）
    @PostMapping("/saveComment")
    public String saveComment(@RequestParam String type, @RequestBody Comment comment) {
        try {
            if ("add".equals(type)) {
                commentService.addComment(comment);
            } else if ("edit".equals(type)) {
                commentService.updateComment(comment);
            }
            return "操作成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "操作失败！";
        }
    }

    // 删除评论
    @PostMapping("/deleteComment")
    public Result deleteComment(@RequestParam(required = false) Integer id) {
        Result result = new Result();
        try {
            if (id == null || id <= 0) {
                result.setErrorMessage("评论ID不能为空");
                return result;
            }
            commentService.deleteComment(id);
        } catch (Exception e) {
            result.setErrorMessage("删除评论失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 根据ID查询评论（编辑回显）
    @PostMapping("/selectById")
    public Result selectById(@RequestParam(required = false) Integer id) {
        Result result = new Result();
        try {
            if (id == null || id <= 0) {
                result.setErrorMessage("评论ID不能为空");
                return result;
            }
            Comment comment = commentService.selectById(id);
            result.getMap().put("comment", comment);
        } catch (Exception e) {
            result.setErrorMessage("查询评论失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 插入评论（供前端直接调用）- 增强：处理回复的replyTo字段
    @PostMapping("/insert")
    public Result insert(@RequestBody Comment comment, HttpServletRequest request) {
        Result result = new Result();
        try {
            // 检查用户是否已登录
            Object userObj = request.getSession().getAttribute("user");
            if (userObj == null) {
                result.setErrorMessage("未登录用户无法评论！");
                return result;
            }

            // 从session中获取用户名和用户ID（保留原有逻辑）
            chh.spring.entity.dto.UserDTO user = (chh.spring.entity.dto.UserDTO) userObj;
            comment.setAuthor(user.getUsername());
            comment.setUserId(user.getId()); // 保留：存入用户ID

            // 新增：如果是回复评论，自动填充被回复的用户名
            if (comment.getParentId() != null && comment.getParentId() > 0) {
                Comment parentComment = commentService.selectById(comment.getParentId());
                if (parentComment != null) {
                    comment.setReplyTo(parentComment.getAuthor());
                }
            }

            commentService.addComment(comment);
            result.getMap().put("comment", comment);
            result.setMsg("添加成功"); // 添加成功提示信息
        } catch (Exception e) {
            result.setErrorMessage("添加评论失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 新增：插入回复评论（供前端直接调用，专门处理回复场景）
    @PostMapping("/insertReply")
    public Result insertReply(@RequestBody Comment comment, HttpServletRequest request) {
        Result result = new Result();
        try {
            // 检查用户是否已登录
            Object userObj = request.getSession().getAttribute("user");
            if (userObj == null) {
                result.setErrorMessage("未登录用户无法评论！");
                return result;
            }

            // 从session中获取用户名和用户ID
            chh.spring.entity.dto.UserDTO user = (chh.spring.entity.dto.UserDTO) userObj;
            comment.setAuthor(user.getUsername());
            comment.setUserId(user.getId()); // 保留：存入用户ID

            // 验证父评论ID是否存在
            if (comment.getParentId() == null || comment.getParentId() <= 0) {
                result.setErrorMessage("父评论ID不能为空！");
                return result;
            }

            // 获取被回复的用户名
            Comment parentComment = commentService.selectById(comment.getParentId());
            if (parentComment == null) {
                result.setErrorMessage("父评论不存在！");
                return result;
            }
            comment.setReplyTo(parentComment.getAuthor());

            commentService.addComment(comment);
            result.getMap().put("comment", comment);
            result.setMsg("回复成功");
        } catch (Exception e) {
            result.setErrorMessage("添加回复失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 按条件搜索评论（管理员功能）
    @PostMapping("/searchComments")
    public Result searchComments(@RequestBody CommentSearch commentSearch) {
        Result result = new Result();
        try {
            // 安全处理可能为null的status属性
            String statusStr = null;
            if (commentSearch.getStatus() != null) {
                statusStr = String.valueOf(commentSearch.getStatus());
                // 检查是否是"undefined"或"null"字符串
                if ("undefined".equals(statusStr) || "null".equals(statusStr)) {
                    statusStr = null;
                }
            }
            
            result = commentService.searchComments(
                    commentSearch.getContent(),
                    commentSearch.getArticleId(),
                    commentSearch.getAuthor(),
                    statusStr,  // 转换为String类型
                    commentSearch.getPageParams()
            );
        } catch (Exception e) {
            result.setErrorMessage("搜索评论失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}