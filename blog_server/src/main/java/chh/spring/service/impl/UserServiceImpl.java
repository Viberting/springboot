package chh.spring.service.impl;

import chh.spring.entity.Authority;
import chh.spring.entity.User;
import chh.spring.entity.UserAuthority;
import chh.spring.entity.vo.UserVO;
import chh.spring.mapper.AuthorityMapper;
import chh.spring.mapper.UserMapper;
import chh.spring.mapper.UserAuthorityMapper;
import chh.spring.service.UserService;
import chh.spring.tools.PageParams;
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
import java.util.stream.Collectors;

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
    private UserMapper userMapper;

    @Autowired
    private AuthorityMapper authorityMapper;
    
    @Autowired
    private UserAuthorityMapper userAuthorityMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Spring Security密码加密器（实验中已配置）

    // 1. 分页查询用户列表
    @Override
    public Result getUserPage(PageParams pageParams) {
        Result result = new Result();
        
        // 分页对象
        Page<UserVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
        IPage<UserVO> userPage = userMapper.getUserPage(page, 1); // 1 表示有效用户

        // 封装结果（用户列表+分页信息）
        pageParams.setTotal(userPage.getTotal());
        result.getMap().put("users", userPage.getRecords());
        result.getMap().put("pageParams", pageParams);
        result.setSuccess(true);
        return result;
    }

    // 2. 新增用户（含权限分配）
    @Override
    public Result addUser(User user, List<Integer> authorityIds) {
        Result result = new Result();
        // 校验用户名唯一性
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        User existingUser = userMapper.selectOne(wrapper);
        if (existingUser != null) {
            result.setErrorMessage("用户名已存在，请更换！");
            return result;
        }
        // 校验密码非空
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.setErrorMessage("密码不能为空！");
            return result;
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认信息
        user.setCreated(LocalDate.now());
        user.setValid(true); // 新增用户默认有效
        // 新增用户到t_user表
        userMapper.insert(user);

        // 确保用户ID已经被设置
        if (user.getId() == null) {
            result.setErrorMessage("用户创建失败，无法获取用户ID");
            return result;
        }

        // 分配权限（批量插入用户-权限关联）
        // 如果没有指定权限，则分配默认权限（普通用户权限，ID=2）
        if (authorityIds == null || authorityIds.isEmpty()) {
            UserAuthority ua = new UserAuthority();
            ua.setUserId(user.getId());
            ua.setAuthorityId(2); // 2=普通用户权限
            userAuthorityMapper.insert(ua);
        } else {
            for (Integer authId : authorityIds) {
                UserAuthority ua = new UserAuthority();
                ua.setUserId(user.getId());
                ua.setAuthorityId(authId);
                userAuthorityMapper.insert(ua);
            }
        }

        result.setSuccess(true);
        result.setMsg("用户新增成功！");
        return result;
    }

    // 3. 编辑用户（含权限更新）
    @Override
    public Result updateUser(User user, List<Integer> authorityIds) {
        Result result = new Result();
        
        // 查询原用户信息
        User oldUser = userMapper.selectById(user.getId());
        if (oldUser == null) {
            result.setErrorMessage("用户不存在！");
            return result;
        }

        // 更新用户信息（只更新允许修改的字段：用户名、邮箱、有效性）
        oldUser.setUsername(user.getUsername());
        oldUser.setEmail(user.getEmail());
        oldUser.setValid(user.getValid());
        // 若密码不为空，则更新密码（为空表示不修改密码）
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            oldUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(oldUser);

        // 更新权限：先删除旧关联，再插入新关联
        QueryWrapper<UserAuthority> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("user_id", user.getId());
        userAuthorityMapper.delete(deleteWrapper);
        
        // 验证权限ID的有效性
        if (authorityIds != null && !authorityIds.isEmpty()) {
            // 检查传入的权限ID是否都存在于权限表中
            for (Integer authId : authorityIds) {
                Authority authority = authorityMapper.selectById(authId);
                if (authority == null) {
                    result.setErrorMessage("权限ID " + authId + " 不存在！");
                    return result;
                }
            }
            
            // 重新插入新的权限关联
            for (Integer authId : authorityIds) {
                UserAuthority ua = new UserAuthority();
                ua.setUserId(user.getId());
                ua.setAuthorityId(authId);
                userAuthorityMapper.insert(ua);
            }
        }

        result.setSuccess(true);
        result.setMsg("用户编辑成功！");
        return result;
    }

    // 4. 删除用户（逻辑删除：修改valid为false）
    @Override
    public Result deleteUser(Integer id) {
        Result result = new Result();
        User user = userMapper.selectById(id);
        if (user == null) {
            result.setErrorMessage("用户不存在！");
            return result;
        }
        // 逻辑删除：不物理删除数据，只标记为无效
        user.setValid(false);
        userMapper.updateById(user);
        result.setSuccess(true);
        result.setMsg("用户删除成功！");
        return result;
    }

    // 5. 根据ID查询用户（编辑回显）
    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    // 6. 查询所有权限（前端下拉选择）
    @Override
    public List<Authority> getAllAuthorities() {
        return authorityMapper.selectList(null);
    }

    // 实现注册方法
    @Override
    public Result register(User user) {
        Result result = new Result();

        // 1. 校验用户名：非空 + 唯一性
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.setErrorMessage("用户名不能为空！");
            return result;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        User existingUser = userMapper.selectOne(wrapper);
        if (existingUser != null) {
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
}