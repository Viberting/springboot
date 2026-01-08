package chh.spring.mapper;

import chh.spring.entity.User;
import chh.spring.entity.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from t_user where username = #{username}")
    public User findByName(String username);
    //根据用户名查询未被禁用的用户信息
    @Select("select * from t_user where username = #{username} and valid = 1")
    public User findByNameWithValid(String username);

    //根据用户名查询用户拥有的权限（t_authority表中的authority）
    //涉及到3张表：t_user,t_authority和t_user_authority
    @Select("select a.authority from t_user u,t_authority a,"+
            "t_user_authority ua where ua.user_id=u.id "+
            "and ua.authority_id=a.id and u.username =#{username}")
    public List<String> findAuthorityByName(String username);

    // 1. 分页查询用户列表（关联权限表，获取用户拥有的权限）
    IPage<UserVO> getUserPage(IPage<UserVO> page, @Param("valid") Object valid, @Param("username") String username, @Param("email") String email, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("ew") Wrapper<UserVO> wrapper);

    // 2. 校验用户名唯一性（新增/编辑用户时用）
    Integer checkUsernameUnique(@Param("username") String username, @Param("id") Integer id);

    // 3. 根据用户ID查询拥有的权限ID
    List<Integer> getAuthorityIdsByUserId(Integer userId);
    
    // 增加关注数
    void incrFollowCount(@Param("userId") Integer userId);
    
    // 增加粉丝数
    void incrFansCount(@Param("userId") Integer userId);
    
    // 减少关注数
    void decrFollowCount(@Param("userId") Integer userId);
    
    // 减少粉丝数
    void decrFansCount(@Param("userId") Integer userId);
}