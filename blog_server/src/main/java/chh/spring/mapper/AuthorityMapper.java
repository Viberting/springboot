package chh.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import chh.spring.entity.Authority;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthorityMapper extends BaseMapper<Authority> {
    // 查询所有权限（用于新增/编辑用户时的权限下拉选择）
    @Select("SELECT * FROM t_authority")
    List<Authority> selectAllAuthorities();
}