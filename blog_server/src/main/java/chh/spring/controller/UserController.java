package chh.spring.controller;

import chh.spring.entity.User;

import chh.spring.entity.vo.UserVO;
import chh.spring.service.UserService;
import chh.spring.mapper.UserAuthorityMapper;
import chh.spring.tools.Result;
import chh.spring.tools.PageParams;
import chh.spring.entity.dto.UserProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Autowired private UserService userService;
    @Autowired private UserAuthorityMapper userAuthorityMapper;
    @Autowired private AuthenticationManager authenticationManager;

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

    // ✅ 核心修复：查询用户+用户的权限ID集合，解决前端回显问题
    @PostMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        try {
            User user = this.userService.getUserById(id);
            if (user != null) {
                Result result = new Result();
                result.setSuccess(true);
                result.getMap().put("user", user);
                // 查询当前用户的所有权限ID
                List<Integer> authorityIds = userAuthorityMapper.selectAuthorityIdsByUserId(id);
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
}