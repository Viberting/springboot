package chh.spring.service.impl;

import chh.spring.entity.Authority;
import chh.spring.entity.User;
import chh.spring.entity.UserAuthority;
import chh.spring.entity.vo.UserVO;
import chh.spring.mapper.AuthorityMapper;
import chh.spring.mapper.UserMapper;
import chh.spring.mapper.UserAuthorityMapper;
import chh.spring.service.UserService;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
@Service
@Transactional // 事务管理：确保用户和权限关联同时创建
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private UserAuthorityMapper userAuthorityMapper;
    
    @Autowired
    private AuthorityMapper authorityMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Spring Security密码加密器（实验中已配置）

    // 实现注册方法
    @Override
    public Result register(User user) {
        Result result = new Result();

        // 1. 校验用户名：非空 + 唯一性
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.setErrorMessage("用户名不能为空！");
            return result;
        }
        Integer count = this.baseMapper.checkUsernameUnique(user.getUsername(), null);
        if (count != null && count > 0) {
            result.setErrorMessage("用户名已存在，请更换！");
            return result;
        }

        // 2. 校验密码：非空（前端已校验，后端二次确认）
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.setErrorMessage("密码不能为空！");
            return result;
        }

        // 3. 密码加密（使用BCrypt算法，实验中Security已配置）
        String encryptedPwd = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPwd); // 替换原明文密码

        // 4. 设置用户默认信息
        user.setCreated(LocalDate.now()); // 注册时间
        user.setValid(true); // 默认账号有效
        user.setEmail(user.getEmail() == null ? "" : user.getEmail()); // 邮箱可选，为空时设空字符串

        // 5. 新增用户到t_user表
        this.save(user);

        // 6. 分配默认角色：普通用户（ROLE_common，对应权限ID=2）
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUserId(user.getId()); // 刚新增用户的ID（自增后自动回显）
        userAuthority.setAuthorityId(2); // 2=普通用户权限（需确保t_authority表有此数据）
        userAuthorityMapper.insert(userAuthority);

        // 7. 注册成功，返回结果
        result.setSuccess(true);
        result.setMsg("注册成功！请登录");
        return result;
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByName(username);
    }
    
    @Override
    public IPage<UserVO> getUserPage(IPage<UserVO> page, Object valid) {
        // 创建查询条件
        QueryWrapper<UserVO> wrapper = new QueryWrapper<>();
        return userMapper.getUserPage(page, valid, wrapper);
    }
    
    @Override
    public List<Authority> getAllAuthorities() {
        return authorityMapper.selectAllAuthorities();
    }
    
    @Override
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }
}