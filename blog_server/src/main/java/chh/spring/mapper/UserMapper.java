package chh.spring.mapper;

import chh.spring.entity.User;
import chh.spring.entity.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    // 根据用户名查询用户
    @Select("select * from t_user where username = #{username}")
    User findByName(String username);

    // 根据用户名查询未被禁用的用户信息
    @Select("select * from t_user where username = #{username} and valid = 1")
    User findByNameWithValid(String username);

    // 根据用户名查询用户拥有的权限（关联3张表）
    @Select("select a.authority from t_user u,t_authority a,"+
            "t_user_authority ua where ua.user_id=u.id "+
            "and ua.authority_id=a.id and u.username =#{username}")
    List<String> findAuthorityByName(String username);

    // 分页查询用户列表（关联权限表）
    @Select("SELECT u.id, u.username, u.email, u.created, " +
            "COALESCE(GROUP_CONCAT(a.authority SEPARATOR ','), '') AS authorityNames " +
            "FROM t_user u " +
            "LEFT JOIN t_user_authority ua ON u.id = ua.user_id " +
            "LEFT JOIN t_authority a ON ua.authority_id = a.id " +
            "WHERE u.valid = #{valid} " +
            "GROUP BY u.id, u.username, u.email, u.created " +
            "ORDER BY u.created DESC " +
            "${ew.customSqlSegment}")
    IPage<UserVO> getUserPage(IPage<UserVO> page, @Param("valid") Object valid, @Param("ew") Wrapper<UserVO> wrapper);

    // 校验用户名唯一性（新增/编辑用户）
    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND id != #{id}")
    Integer checkUsernameUnique(@Param("username") String username, @Param("id") Integer id);

    // 根据用户ID查询拥有的权限ID
    @Select("SELECT authority_id FROM t_user_authority WHERE user_id = #{userId}")
    List<Integer> getAuthorityIdsByUserId(@Param("userId") Integer userId);

    // ========== 关注/粉丝数更新方法 ==========
    // 关注数+1（补充@Param注解，避免参数绑定失败）
    @Update("UPDATE t_user SET follow_count = follow_count + 1 WHERE id = #{userId}")
    int incrFollowCount(@Param("userId") Integer userId);

    // 关注数-1（补充@Param注解，且限制follow_count>0避免负数）
    @Update("UPDATE t_user SET follow_count = follow_count - 1 WHERE id = #{userId} AND follow_count > 0")
    int decrFollowCount(@Param("userId") Integer userId);

    // 粉丝数+1（补充@Param注解）
    @Update("UPDATE t_user SET fans_count = fans_count + 1 WHERE id = #{userId}")
    int incrFansCount(@Param("userId") Integer userId);

    // 粉丝数-1（补充@Param注解，限制fans_count>0）
    @Update("UPDATE t_user SET fans_count = fans_count - 1 WHERE id = #{userId} AND fans_count > 0")
    int decrFansCount(@Param("userId") Integer userId);
}