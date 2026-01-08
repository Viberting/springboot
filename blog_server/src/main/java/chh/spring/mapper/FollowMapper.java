package chh.spring.mapper;

import chh.spring.entity.Follow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    // 自定义物理删除方法
    @Delete("DELETE FROM t_follow WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    void deletePhysically(@Param("followerId") Integer followerId, @Param("followedId") Integer followedId);
}