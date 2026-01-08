package chh.spring.controller;

import chh.spring.service.UserService;
import chh.spring.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserImageController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/uploadImage")
    public Result uploadImage(MultipartFile file) {
        Result result = new Result();
        try {
            String url = userService.uploadImage(file);
            result.setSuccess(true);
            result.getMap().put("url", url);
        } catch (Exception e) {
            result.setErrorMessage("上传失败！");
            e.printStackTrace();
        }
        return result;
    }
}