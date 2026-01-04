package chh.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import chh.spring.entity.UserAuthority;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAuthorityMapper extends BaseMapper<UserAuthority> {
    // 继承BaseMapper，无需额外方法（直接用insert新增关联关系）
    // 根据用户ID删除所有关联的权限（编辑用户权限时先清空旧关联）
    @Delete("DELETE FROM t_user_authority WHERE user_id = #{userId}")
    void deleteByUserId(Integer userId);

    // 批量插入用户-权限关联（编辑用户权限时用）
    void batchInsert(@Param("list") List<UserAuthority> userAuthorityList);
}