package chh.spring.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import chh.spring.entity.Article;
import chh.spring.entity.User;
import chh.spring.entity.dto.UserProfileDTO;
import chh.spring.service.ArticleService;
import chh.spring.service.UserService;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;

@RestController
@RequestMapping("/user") // 接口前缀
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ArticleService articleService;

    // 新增：用户注册接口
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            return userService.register(user);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("注册失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 更新用户资料接口
    @PutMapping("/profile/{userId}")
    public Result updateProfile(@PathVariable Integer userId, @RequestBody UserProfileDTO profileDTO) {
        try {
            userService.updateUserProfile(userId, profileDTO);
            Result result = new Result();
            result.setSuccess(true);
            result.setMsg("更新成功");
            return result;
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("更新失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    // 获取当前用户资料接口
    @GetMapping("/profile")
    public Result getCurrentUserProfile() {
        try {
            // 从安全上下文获取当前认证的用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                // 从数据库获取完整的用户信息
                String username = authentication.getName();
                User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
                
                if (user != null) {
                    Result result = new Result();
                    result.setSuccess(true);
                    result.setMsg("获取用户信息成功");
                    result.getMap().put("user", user);
                    return result;
                } else {
                    Result result = new Result();
                    result.setErrorMessage("用户不存在");
                    return result;
                }
            } else {
                Result result = new Result();
                result.setErrorMessage("用户未登录");
                return result;
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("获取用户信息失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    // 根据ID获取用户信息接口
    @GetMapping("/{userId}")
    public Result getUserById(@PathVariable Integer userId) {
        try {
            User user = userService.getById(userId);
            if (user != null) {
                Result result = new Result();
                result.setSuccess(true);
                result.setMsg("获取用户信息成功");
                result.getMap().put("user", user);
                return result;
            } else {
                Result result = new Result();
                result.setErrorMessage("用户不存在");
                return result;
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("获取用户信息失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    // 根据用户ID获取文章列表
    @GetMapping("/{userId}/articles")
    public Result getUserArticles(@PathVariable Integer userId, 
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 检查用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                Result result = new Result();
                result.setErrorMessage("用户不存在");
                return result;
            }
            
            // 创建分页参数
            PageParams pageParams = new PageParams();
            pageParams.setPage(page.longValue());  // 转换为Long类型
            pageParams.setRows(size.longValue());  // 转换为Long类型
            
            // 获取用户的文章列表
            Result resultFromService = articleService.getArticlesByAuthorId(userId, pageParams);
            
            if (resultFromService.isSuccess()) {
                Result result = new Result();
                result.setSuccess(true);
                result.setMsg("获取用户文章列表成功");
                
                // 从服务返回的结果中提取数据
                if (resultFromService.getMap().containsKey("articles")) {
                    result.getMap().put("articles", resultFromService.getMap().get("articles"));
                }
                if (resultFromService.getMap().containsKey("pageParams")) {
                    result.getMap().put("pageParams", resultFromService.getMap().get("pageParams"));
                }
                
                return result;
            } else {
                return resultFromService; // 直接返回服务层的错误结果
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("获取用户文章列表失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
}