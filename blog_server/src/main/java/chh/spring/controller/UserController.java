package chh.spring.controller;

import chh.spring.entity.User;

import chh.spring.entity.vo.UserVO;
import chh.spring.mapper.UserMapper;
import chh.spring.service.ArticleService;
import chh.spring.service.FollowService;
import chh.spring.service.UserService;
import chh.spring.mapper.UserAuthorityMapper;
import chh.spring.tools.Result;
import chh.spring.tools.PageParams;
import chh.spring.entity.dto.UserProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import javax.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService; // 注入文章服务
    @Autowired
    private UserAuthorityMapper userAuthorityMapper;
    // 注入密码加密器（Spring Security配置的BCryptPasswordEncoder）
    @Autowired
    private PasswordEncoder passwordEncoder;
    // ========== 新增：注入FollowService ==========
    @Autowired
    private FollowService followService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try { return userService.register(user); }
        catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("注册失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    @GetMapping("/profile")
    public Result getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = this.userService.findByUsername(username);
            if (user != null) {
                UserProfileDTO profileDTO = new UserProfileDTO();
                profileDTO.setUsername(user.getUsername());
                profileDTO.setEmail(user.getEmail());
                profileDTO.setAvatar(user.getAvatar());
                profileDTO.setIntro(user.getIntro());
                profileDTO.setGithubUrl(user.getGithubUrl());
                profileDTO.setBgImage(user.getBgImage());
                profileDTO.setGender(user.getGender());
                profileDTO.setBirthday(user.getBirthday());
                Result result = new Result();
                result.setSuccess(true);
                result.getMap().put("userProfile", profileDTO);
                return result;
            } else {
                Result result = new Result();
                result.setErrorMessage("用户不存在");
                return result;
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("获取用户资料失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    @PostMapping("/getUserPage")
    public Result getUserPage(@RequestBody PageParams pageParams) {
        try {
            // 1. 创建分页对象，页码+条数
            IPage<UserVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
            // 2. 核心修复：t_user.valid是tinyint(1) 数值类型，传 Integer 0/1，不要传Boolean/null
            // 传null会导致查询条件拼接错误，直接查不到数据，这里给默认值1=查询有效用户
            Integer validStatus = pageParams.getValid() == null ? 1 : Integer.valueOf(String.valueOf(pageParams.getValid()));
            // 使用新的搜索方法
            IPage<UserVO> userPage = this.userService.getUserPageWithSearch(page, validStatus, 
                pageParams.getUsername(), pageParams.getEmail(), 
                pageParams.getStartDate(), pageParams.getEndDate());

            Result result = new Result();
            result.setSuccess(true);
            // 3. 强制赋值，保证前端一定能拿到这两个字段，不会undefined
            pageParams.setTotal(userPage.getTotal());
            result.getMap().put("userVOs", userPage.getRecords());
            result.getMap().put("pageParams", pageParams);

            System.out.println("=====调试日志=====");
            System.out.println("查询总数："+userPage.getTotal());
            System.out.println("查询数据："+userPage.getRecords());
            return result;
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询用户列表失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    @PostMapping("/getAllAuthorities")
    public Result getAllAuthorities() {
        try {
            Result result = new Result();
            result.setSuccess(true);
            result.getMap().put("authorities", userService.getAllAuthorities());
            return result;
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("获取权限列表失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 新增：更新用户个人资料接口
    @PostMapping("/updateProfile")
    public Result updateProfile(@RequestBody UserProfileDTO profileDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = this.userService.findByUsername(username);
            if (user != null) {
                user.setAvatar(profileDTO.getAvatar());
                user.setIntro(profileDTO.getIntro());
                user.setGithubUrl(profileDTO.getGithubUrl());
                user.setBgImage(profileDTO.getBgImage());
                user.setGender(profileDTO.getGender());
                user.setBirthday(profileDTO.getBirthday());
                user.setEmail(profileDTO.getEmail());
                boolean updated = this.userService.updateById(user);
                Result result = new Result();
                if (updated) {
                    result.setSuccess(true);result.setMsg("资料更新成功");
                } else { result.setErrorMessage("资料更新失败");}
                return result;
            } else {
                Result result = new Result();result.setErrorMessage("用户不存在");
                return result;
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("更新用户资料失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 新增：GET版本（个人主页用）
    @GetMapping("/selectById/{id}")
    public Result selectByIdGet(@PathVariable Integer id) {
        // 复用原有逻辑，直接调用上面的核心逻辑
        return selectById(id, null);
    }

    // 原有POST版本（用户管理用）
    @PostMapping("/selectById")
    public Result selectByIdPost(
            @RequestParam(required = false) Integer id,
            @RequestBody(required = false) Map<String, Integer> params
    ) {
        Integer targetId = params != null ? params.get("id") : id;
        return selectById(targetId, null);
    }

    // 抽离核心逻辑（复用）
    private Result selectById(Integer targetId, Map<String, Integer> params) {
        // 把上面的try-catch逻辑移到这里，实现代码复用
        try {
            if (targetId == null) {
                Result result = new Result();
                result.setErrorMessage("用户ID不能为空");
                return result;
            }
            User user = this.userService.getUserById(targetId);
            if (user != null) {
                Result result = new Result();
                result.setSuccess(true);
                result.getMap().put("user", user);
                List<Integer> authorityIds = userAuthorityMapper.selectAuthorityIdsByUserId(targetId);
                result.getMap().put("authorityIds", authorityIds);
                return result;
            } else {
                Result result = new Result();
                result.setErrorMessage("用户不存在");
                return result;
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询用户详情失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    @PostMapping("/login")
    public Result login(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Result result = new Result();
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(token);
            if (authentication.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                User user = this.userService.findByUsername(username);
                if (user != null) {
                    UserProfileDTO profileDTO = new UserProfileDTO();
                    profileDTO.setUsername(user.getUsername());
                    profileDTO.setEmail(user.getEmail());
                    result.setSuccess(true);
                    result.getMap().put("user", profileDTO);
                    result.setMsg("登录成功");
                } else { result.setErrorMessage("用户不存在");}
            } else { result.setErrorMessage("登录失败");}
        } catch (Exception e) {
            result.setErrorMessage("用户名或密码错误");
        }
        return result;
    }

    // 修改：管理员更新用户信息，权限默认为普通用户
    @PostMapping("/adminUpdateUser")
    public Result adminUpdateUser(@RequestBody User user) {
        try {
            Result result = userService.adminUpdateUser(user);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Result result = new Result();
            result.setErrorMessage("更新用户失败：" + e.getMessage());
            return result;
        }
    }

    // 修改：管理员新增用户，权限默认为普通用户
    @PostMapping("/adminAddUser")
    public Result adminAddUser(@RequestBody User user) {
        try {
            Result result = userService.adminAddUser(user);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Result result = new Result();
            result.setErrorMessage("新增用户失败：" + e.getMessage());
            return result;
        }
    }

    // ✅ 新增：管理员删除/禁用用户接口 【前端调用的就是这个接口，之前缺失】
    @PostMapping("/deleteUser")
    public Result deleteUser(@RequestParam Integer id) {
        try {
            User user = userService.getUserById(id);
            if(user == null){
                return new Result(false, "用户不存在");
            }
            // 逻辑删除：不是删除数据，而是把账号状态改为禁用 valid=0
            user.setValid(0);
            boolean update = userService.updateById(user);
            if(update){
                return new Result(true, "用户禁用成功");
            }else{
                return new Result(false, "用户禁用失败");
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("禁用用户失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // ========== 简化：用户统计数据接口（只查文章数，关注/粉丝显示0） ==========
    @GetMapping("/{userId}/stats")
    public Result getUserStats(@PathVariable Integer userId) {
        Result result = new Result();
        try {
            // 复用你已有的ArticleService方法：根据作者ID查文章列表，取总数
            PageParams pageParams = new PageParams();
            pageParams.setPage(1L);
            pageParams.setRows(10L);
            Result articleResult = articleService.getArticlesByAuthorId(userId, pageParams);

            // 从返回结果中取文章总数
            int articles = 0;
            if (articleResult.getMap().containsKey("pageParams")) {
                PageParams resPageParams = (PageParams) articleResult.getMap().get("pageParams");
                articles = Math.toIntExact(resPageParams.getTotal());
            }

            // 关注/粉丝数先显示0（后续实现关注功能后再改）
            int following = 0;
            int followers = 0;

            result.setSuccess(true);
            result.getMap().put("articles", articles);
            result.getMap().put("following", following);
            result.getMap().put("followers", followers);
        } catch (Exception e) {
            result.setErrorMessage("获取用户统计数据失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // ==========用户文章列表接口 ==========
    @GetMapping("/{userId}/articles")
    public Result getUserArticles(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer page, // 给默认值，避免空指针
            @RequestParam(defaultValue = "5") Integer size) { // 给默认值
        Result result = new Result();
        try {
            PageParams pageParams = new PageParams();
            // 直接用Integer，避免Long转换的潜在问题
            pageParams.setPage(Long.valueOf(page));
            pageParams.setRows(Long.valueOf(size));

            Result articleResult = articleService.getArticlesByAuthorId(userId, pageParams);
            if (articleResult.isSuccess()) { // 新增：判断文章查询是否成功
                result.setSuccess(true);
                result.getMap().put("articles", articleResult.getMap().get("articles"));
                result.getMap().put("pageParams", articleResult.getMap().get("pageParams"));
            } else {
                result.setErrorMessage(articleResult.getMsg());
            }
        } catch (Exception e) {
            result.setErrorMessage("获取文章失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 新增：密码修改接口
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody Map<String, String> passwordMap) {
        try {
            // 1. 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user == null) {
                Result result = new Result();
                result.setErrorMessage("用户不存在");
                return result;
            }

            // 2. 获取请求参数
            String oldPassword = passwordMap.get("oldPassword");
            String newPassword = passwordMap.get("newPassword");

            // 3. 验证原密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                Result result = new Result();
                result.setErrorMessage("原密码错误");
                return result;
            }

            // 4. 加密新密码并更新
            user.setPassword(passwordEncoder.encode(newPassword));
            boolean updated = userService.updateById(user);

            Result result = new Result();
            if (updated) {
                result.setSuccess(true);
                result.setMsg("密码修改成功");
            } else {
                result.setErrorMessage("密码修改失败");
            }
            return result;
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("密码修改异常：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // ==========用户全量统计接口（新增） ==========
    @GetMapping("/{userId}/fullStats")
    public Result getUserFullStats(@PathVariable Integer userId) {
        Result result = new Result();
        try {
            // 1. 获取文章总数（复用原有文章分页接口的逻辑，取总数）
            PageParams pageParams = new PageParams();
            pageParams.setPage(1L);
            pageParams.setRows(1L); // 只查1条，只为获取总数
            Result articleResult = articleService.getArticlesByAuthorId(userId, pageParams);
            int articleCount = 0;
            if (articleResult.isSuccess()) {
                PageParams pageData = (PageParams) articleResult.getMap().get("pageParams");
                articleCount = (pageData.getTotal() != null && pageData.getTotal() > 0)
                        ? pageData.getTotal().intValue()
                        : 0;
            }

            // 2. 获取关注数/粉丝数（调用FollowService）
            Map<String, Integer> followCountMap = followService.getFollowAndFansCount(userId);
            int followCount = followCountMap.getOrDefault("followCount", 0);
            int fansCount = followCountMap.getOrDefault("fansCount", 0);

            // 3. 封装返回数据
            result.setSuccess(true);
            result.getMap().put("articleCount", articleCount); // 文章总数
            result.getMap().put("followCount", followCount);   // 关注数
            result.getMap().put("fansCount", fansCount);       // 粉丝数
        } catch (Exception e) {
            result.setErrorMessage("获取统计数据失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}