package chh.spring.config.security;

import chh.spring.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override//用于查找用户及其密码、权限等信息
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //此处有2个User类，注意区分，此处的User类为代表数据库t_user表的User类，不是Spring Security的User类
        chh.spring.entity.User user = userMapper.findByNameWithValid(username);

        if (null == user)
            throw new UsernameNotFoundException(username);
            
        // 检查用户是否有效，无效用户不能登录
        if (!user.getValid()) {
            throw new DisabledException("用户已被禁用，无法登录");
        }

        //查找用户拥有的权限（角色）
        List<String> authorityNames = userMapper.findAuthorityByName(username);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String authorityName : authorityNames) {
            // 确保权限名称正确添加
            authorities.add(new SimpleGrantedAuthority(authorityName));
        }
        //此处的User类为Spring Security的User类,Security会使用这里的用户和权限信息判断是否能访问某些资源
        return new User(user.getUsername(), user.getPassword(), authorities);
    }

}
