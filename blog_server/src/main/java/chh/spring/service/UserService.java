package chh.spring.service;

import chh.spring.entity.Authority;
import chh.spring.entity.User;
import chh.spring.entity.vo.UserVO;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
public interface UserService extends IService<User> {

    // 新增：用户注册
    Result register(User user);
    
    // 查询用户信息（通过用户名）
    User findByUsername(String username);
    
    // 分页查询用户列表
    IPage<UserVO> getUserPage(IPage<UserVO> page, Object valid);
    
    // 获取所有权限
    List<Authority> getAllAuthorities();
    
    // 根据ID查询用户详情
    User getUserById(Integer id);
    
    // 上传图片
    String uploadImage(MultipartFile file);
}