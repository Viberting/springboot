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
    public Result getAPageCommentByArticleId(@RequestParam Integer articleId,
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
    public Result deleteComment(@RequestParam Integer id) {
        Result result = new Result();
        try {
            commentService.deleteComment(id);
        } catch (Exception e) {
            result.setErrorMessage("删除评论失败！");
            e.printStackTrace();
        }
        return result;
    }

    // 根据ID查询评论（编辑回显）
    @PostMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        Result result = new Result();
        try {
            Comment comment = commentService.selectById(id);
            result.getMap().put("comment", comment);
        } catch (Exception e) {
            result.setErrorMessage("查询评论失败！");
            e.printStackTrace();
        }
        return result;
    }

    // 插入评论（供前端直接调用）
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
            
            // 从session中获取用户名
            chh.spring.entity.dto.UserDTO user = (chh.spring.entity.dto.UserDTO) userObj;
            comment.setAuthor(user.getUsername());
            
            commentService.addComment(comment);
            result.getMap().put("comment", comment);
            result.setMsg("添加成功"); // 添加成功提示信息
        } catch (Exception e) {
            result.setErrorMessage("添加评论失败！");
            e.printStackTrace();
        }
        return result;
    }

    // 按条件搜索评论（管理员功能）
    @PostMapping("/searchComments")
    public Result searchComments(@RequestBody CommentSearch commentSearch) {
        Result result = new Result();
        try {
            result = commentService.searchComments(
                    commentSearch.getContent(),
                    commentSearch.getArticleId(),
                    commentSearch.getAuthor(),
                    String.valueOf(commentSearch.getStatus()),  // 转换为String类型
                    commentSearch.getPageParams()
            );
        } catch (Exception e) {
            result.setErrorMessage("搜索评论失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}