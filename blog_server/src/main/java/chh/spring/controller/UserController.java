package chh.spring.controller;

import chh.spring.entity.Authority;
import chh.spring.entity.dto.UserDTO;
import chh.spring.tools.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import chh.spring.entity.User;
import chh.spring.mapper.UserMapper;
import chh.spring.service.UserService;
import chh.spring.tools.Result;

import java.util.List;

@RestController
@RequestMapping("/user") // 接口前缀
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;

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

    // 1. 分页查询用户列表
    @PostMapping("/getUserPage")
    public Result getUserPage(@RequestBody PageParams pageParams) {
        try {
            return userService.getUserPage(pageParams);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("查询用户列表失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 2. 新增用户
    @PostMapping("/addUser")
    public Result addUser(@RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            UserDTO.dtoToEntity(userDTO, user);
            return userService.addUser(user, userDTO.getAuthorityIds());
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("新增用户失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 3. 编辑用户
    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            UserDTO.dtoToEntity(userDTO, user);
            return userService.updateUser(user, userDTO.getAuthorityIds());
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("编辑用户失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 4. 删除用户
    @PostMapping("/deleteUser")
    public Result deleteUser(@RequestParam Integer id) {
        try {
            return userService.deleteUser(id);
        } catch (Exception e) {
            Result result = new Result();
            result.setErrorMessage("删除用户失败：" + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    // 5. 根据ID查询用户（编辑回显）
    @PostMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        Result result = new Result();
        try {
            User user = userService.selectById(id);
            result.getMap().put("user", user);
            // 同时查询用户拥有的权限ID（回显权限选择）
            List<Integer> authorityIds = userMapper.getAuthorityIdsByUserId(id);
            result.getMap().put("authorityIds", authorityIds);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setErrorMessage("查询用户失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 6. 查询所有权限（前端下拉选择）
    @PostMapping("/getAllAuthorities")
    public Result getAllAuthorities() {
        Result result = new Result();
        try {
            List<Authority> authorities = userService.getAllAuthorities();
            result.getMap().put("authorities", authorities);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setErrorMessage("查询权限列表失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}