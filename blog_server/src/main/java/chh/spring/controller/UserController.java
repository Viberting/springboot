package chh.spring.controller;

import chh.spring.entity.dto.UserProfileDTO;
import chh.spring.entity.vo.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import chh.spring.entity.Authority;
import chh.spring.entity.User;
import chh.spring.service.UserService;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import chh.spring.mapper.UserMapper;

@RestController
@RequestMapping("/user") // 接口前缀
public class UserController {

    @Autowired
    private UserService userService;

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

    // 新增：用户个人资料接口
    @GetMapping("/profile")
    public Result getUserProfile() {
        try {
            // 从SecurityContext获取当前用户信息
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
    
    // 新增：分页查询用户列表接口
    @PostMapping("/getUserPage")
    public Result getUserPage(@RequestBody PageParams pageParams) {
        try {
            // 创建分页对象
            IPage<UserVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
            
            // 调用服务层分页查询方法
            IPage<UserVO> userPage = this.userService.getUserPage(page, 1); // 1表示有效用户，对应Boolean类型true
            
            Result result = new Result();
            result.setSuccess(true);
            
            // 按照ArticleController的模式返回数据
            pageParams.setTotal(userPage.getTotal());
            result.getMap().put("userVOs", userPage.getRecords());
            result.getMap().put("pageParams", pageParams);
            
            return result;
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询用户列表失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    // 新增：获取所有权限列表接口
    @PostMapping("/getAllAuthorities")
    public Result getAllAuthorities() {
        try {
            List<Authority> authorities = this.userService.getAllAuthorities();
            
            Result result = new Result();
            result.setSuccess(true);
            result.getMap().put("authorities", authorities);
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
                if (updated) {
                    Result result = new Result();
                    result.setSuccess(true);
                    result.setMsg("资料更新成功");
                    return result;
                } else {
                    Result result = new Result();
                    result.setErrorMessage("资料更新失败");
                    return result;
                }
            } else {
                Result result = new Result();
                result.setErrorMessage("用户不存在");
                return result;
            }
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("更新用户资料失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    // 新增：根据ID查询用户详情接口
    @PostMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        try {
            User user = this.userService.getUserById(id);
            if (user != null) {
                Result result = new Result();
                result.setSuccess(true);
                result.getMap().put("user", user);
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
    
    // 新增：用户登录接口
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
                
                // 获取用户信息
                User user = this.userService.findByUsername(username);
                if (user != null) {
                    // 获取用户权限
                    List<String> authorities = userMapper.findAuthorityByName(username);
                    
                    UserProfileDTO profileDTO = new UserProfileDTO();
                    profileDTO.setUsername(user.getUsername());
                    profileDTO.setEmail(user.getEmail());
                    profileDTO.setAvatar(user.getAvatar());
                    profileDTO.setIntro(user.getIntro());
                    profileDTO.setGithubUrl(user.getGithubUrl());
                    profileDTO.setBgImage(user.getBgImage());
                    profileDTO.setGender(user.getGender());
                    profileDTO.setBirthday(user.getBirthday());
                    
                    result.setSuccess(true);
                    result.getMap().put("user", profileDTO);
                    result.getMap().put("authorities", authorities);
                    result.setMsg("登录成功");
                } else {
                    result.setErrorMessage("用户不存在");
                }
            } else {
                result.setErrorMessage("登录失败");
            }
        } catch (AuthenticationException e) {
            result.setErrorMessage("用户名或密码错误");
        } catch (Exception e) {
            result.setErrorMessage("登录异常：" + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserMapper userMapper;
}
