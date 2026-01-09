package chh.spring.controller;

import chh.spring.entity.Comment;
import chh.spring.service.CommentService;
import chh.spring.tools.CommentSearch;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理控制器 - 专用于管理员后台评论管理
 */
@RestController
@RequestMapping("/admin/comment")
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 分页查询所有评论（管理员专用）
     */
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

    /**
     * 删除评论（管理员功能）
     */
    @PostMapping("/deleteComment")
    public Result deleteComment(@RequestParam(required = false) Integer id) {
        Result result = new Result();
        try {
            if (id == null || id <= 0) {
                result.setErrorMessage("评论ID不能为空");
                return result;
            }
            commentService.deleteComment(id);
            result.setMsg("删除成功");
        } catch (Exception e) {
            result.setErrorMessage("删除评论失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 批量删除评论
     */
    @PostMapping("/batchDeleteComment")
    public Result batchDeleteComment(@RequestBody Integer[] ids) {
        Result result = new Result();
        try {
            if (ids != null) {
                for (Integer id : ids) {
                    if (id != null && id > 0) {
                        commentService.deleteComment(id);
                    }
                }
            }
            result.setMsg("批量删除成功");
        } catch (Exception e) {
            result.setErrorMessage("批量删除评论失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更新评论状态（审核/驳回）
     */
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestParam(required = false) Integer id, @RequestParam String status) {
        Result result = new Result();
        try {
            if (id == null || id <= 0) {
                result.setErrorMessage("评论ID不能为空");
                return result;
            }
            Comment comment = commentService.selectById(id);
            if (comment != null) {
                comment.setStatus(status);
                commentService.updateComment(comment);
                result.setMsg("状态更新成功");
            } else {
                result.setErrorMessage("评论不存在");
            }
        } catch (Exception e) {
            result.setErrorMessage("更新状态失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按条件搜索评论（管理员功能）
     */
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
                    statusStr,
                    commentSearch.getPageParams()
            );
        } catch (Exception e) {
            result.setErrorMessage("搜索评论失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable String id) {
        Result result = new Result();
        try {
            Integer commentId = null;
            if (id != null && !"undefined".equals(id) && !"null".equals(id)) {
                try {
                    commentId = Integer.valueOf(id);
                } catch (NumberFormatException e) {
                    result.setErrorMessage("评论ID格式不正确");
                    return result;
                }
            }
            
            if (commentId == null || commentId <= 0) {
                result.setErrorMessage("评论ID不能为空或无效");
                return result;
            }
            
            Comment comment = commentService.selectById(commentId);
            result.getMap().put("comment", comment);
        } catch (Exception e) {
            result.setErrorMessage("查询评论详情失败！" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}