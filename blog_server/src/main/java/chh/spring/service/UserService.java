package chh.spring.service;

import chh.spring.entity.Authority;
import chh.spring.entity.User;
import chh.spring.entity.vo.UserVO;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface UserService extends IService<User> {

    // 新增：用户注册
    Result register(User user);
    
    // 查询用户信息（通过用户名）
    User findByUsername(String username);
    
    // 分页查询用户列表
    IPage<UserVO> getUserPage(IPage<UserVO> page, Object valid);
    
    // 新增：带搜索条件的分页查询用户列表
    IPage<UserVO> getUserPageWithSearch(IPage<UserVO> page, Object valid, String username, String email, LocalDate startDate, LocalDate endDate);
    
    // 获取所有权限
    List<Authority> getAllAuthorities();
    
    // 根据ID查询用户详情
    User getUserById(Integer id);
    
    // 管理员更新用户信息
    Result adminUpdateUser(User user);
    
    // 管理员新增用户
    Result adminAddUser(User user);
    
    // 上传图片
    String uploadImage(MultipartFile file);
}