package chh.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import chh.spring.entity.User;
import chh.spring.service.UserService;
import chh.spring.tools.Result;

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

    // 原有方法（如查询用户等，若有）
    // ...
}
