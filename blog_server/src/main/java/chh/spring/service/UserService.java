package chh.spring.service;

import chh.spring.entity.Authority;
import chh.spring.entity.User;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.extension.service.IService;

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
    // 1. 分页查询用户列表
    Result getUserPage(PageParams pageParams);

    // 2. 新增用户（含权限分配）
    Result addUser(User user, List<Integer> authorityIds);

    // 3. 编辑用户（含权限更新）
    Result updateUser(User user, List<Integer> authorityIds);

    // 4. 删除用户（逻辑删除：修改valid为false）
    Result deleteUser(Integer id);

    // 5. 根据ID查询用户（用于编辑回显）
    User selectById(Integer id);

    // 6. 查询所有权限（用于前端下拉选择）
    List<Authority> getAllAuthorities();
}
