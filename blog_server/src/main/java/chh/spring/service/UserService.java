package chh.spring.service;

import chh.spring.entity.User;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
