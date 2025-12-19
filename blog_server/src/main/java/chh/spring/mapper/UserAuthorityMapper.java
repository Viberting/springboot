package chh.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import chh.spring.entity.UserAuthority;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAuthorityMapper extends BaseMapper<UserAuthority> {
    // 继承BaseMapper，无需额外方法（直接用insert新增关联关系）
}