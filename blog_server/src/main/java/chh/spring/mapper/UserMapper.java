package chh.spring.mapper;

import chh.spring.entity.User;
import chh.spring.entity.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
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
    @Select("SELECT u.id, u.username, u.email, u.created, " +
            "GROUP_CONCAT(a.authority) AS authorityNames " +
            "FROM t_user u " +
            "LEFT JOIN t_user_authority ua ON u.id = ua.user_id " +
            "LEFT JOIN t_authority a ON ua.authority_id = a.id " +
            "WHERE u.valid = #{valid} " +
            "GROUP BY u.id " +
            "ORDER BY u.created DESC")
    IPage<UserVO> getUserPage(IPage<UserVO> page, @Param("valid") Integer valid);

    // 2. 校验用户名唯一性（新增/编辑用户时用）
    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND id != #{id}")
    Integer checkUsernameUnique(@Param("username") String username, @Param("id") Integer id);

    // 3. 根据用户ID查询拥有的权限ID
    @Select("SELECT authority_id FROM t_user_authority WHERE user_id = #{userId}")
    List<Integer> getAuthorityIdsByUserId(Integer userId);
}