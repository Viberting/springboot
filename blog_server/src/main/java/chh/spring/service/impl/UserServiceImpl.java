package chh.spring.service.impl;

import chh.spring.entity.User;
import chh.spring.entity.UserAuthority;
import chh.spring.entity.Authority;
import chh.spring.entity.vo.UserVO;
import chh.spring.mapper.UserMapper;
import chh.spring.mapper.UserAuthorityMapper;
import chh.spring.mapper.AuthorityMapper;
import chh.spring.service.UserService;
import chh.spring.tools.Result;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.time.LocalDate;
import java.util.List;

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
        user.setValid(1); // 默认账号有效
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
        User user = userMapper.findByName(username);
        if (user != null) {
            // 设置默认头像，如果头像字段为空或null
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                user.setAvatar("/api/images/avatars.jpg"); // 设置默认头像
            }
        }
        return user;
    }
    
    @Override
    public IPage<UserVO> getUserPage(IPage<UserVO> page, Object valid) {
        // 创建查询条件
        QueryWrapper<UserVO> wrapper = new QueryWrapper<>();
        // 从PageParams中获取搜索条件，但这里需要修改方法签名以支持搜索参数
        return userMapper.getUserPage(page, valid, null, null, null, null, wrapper);
    }
    
    @Override
    public IPage<UserVO> getUserPageWithSearch(IPage<UserVO> page, Object valid, String username, String email, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<UserVO> wrapper = new QueryWrapper<>();
        return userMapper.getUserPage(page, valid, username, email, startDate, endDate, wrapper);
    }
    
    @Override
    public List<Authority> getAllAuthorities() {
        return authorityMapper.selectAllAuthorities();
    }
    
    @Override
    public User getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            // 设置默认头像，如果头像字段为空或null
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                user.setAvatar("/api/images/avatars.jpg"); // 设置默认头像
            }
        }
        return user;
    }
    
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // 获取上传目录（从application.yml配置）
            String uploadDir = "E:\\img\\images\\";
            
            // 创建目录（如果不存在）
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL（根据您的实际部署情况调整）
            return "/api/images/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }
    }
    
    @Override
    public Result adminAddUser(User user) {
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

        // 2. 校验密码：非空
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.setErrorMessage("密码不能为空！");
            return result;
        }

        // 3. 密码加密
        String encryptedPwd = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPwd);

        // 4. 设置用户默认信息
        user.setCreated(LocalDate.now());
        user.setValid(1); // 默认账号有效
        user.setEmail(user.getEmail() == null ? "" : user.getEmail());

        // 5. 新增用户到t_user表
        this.save(user);

        // 6. 默认分配普通用户权限（ID=2）
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUserId(user.getId());
        userAuthority.setAuthorityId(2); // 普通用户权限
        userAuthorityMapper.insert(userAuthority);

        result.setSuccess(true);
        result.setMsg("用户创建成功！");
        return result;
    }
    
    @Override
    public Result adminUpdateUser(User user) {
        Result result = new Result();

        // 1. 获取原始用户信息
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            result.setErrorMessage("用户不存在！");
            return result;
        }

        // 2. 如果用户名有变化，需要校验新用户名的唯一性
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            // 用户名有变化时，校验新用户名的唯一性（排除当前用户）
            if (!existingUser.getUsername().equals(user.getUsername())) {
                Integer count = this.baseMapper.checkUsernameUnique(user.getUsername(), user.getId());
                if (count != null && count > 0) {
                    result.setErrorMessage("用户名已存在，请更换！");
                    return result;
                }
                // 设置新的用户名
                existingUser.setUsername(user.getUsername());
            }
        }
        
        // 3. 如果提供了新密码，则加密更新
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            String encryptedPwd = passwordEncoder.encode(user.getPassword());
            existingUser.setPassword(encryptedPwd);
        }
        
        // 4. 更新其他可修改的字段
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getValid() != null) {
            existingUser.setValid(user.getValid());
        }

        // 5. 更新用户信息
        boolean userUpdated = this.updateById(existingUser);
        if (!userUpdated) {
            result.setErrorMessage("更新用户信息失败！");
            return result;
        }

        // 6. 删除原有权限关联
        LambdaQueryWrapper<UserAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAuthority::getUserId, existingUser.getId());
        userAuthorityMapper.delete(queryWrapper);

        // 7. 重新分配权限 - 默认分配普通用户权限（ID=2）
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUserId(existingUser.getId());
        userAuthority.setAuthorityId(2); // 普通用户权限
        userAuthorityMapper.insert(userAuthority);

        result.setSuccess(true);
        result.setMsg("用户更新成功！");
        return result;
    }
}