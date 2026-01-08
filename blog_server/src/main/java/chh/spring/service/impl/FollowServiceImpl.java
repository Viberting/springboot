package chh.spring.service.impl;

import chh.spring.entity.Follow;
import chh.spring.entity.User;
import chh.spring.mapper.FollowMapper;
import chh.spring.mapper.UserMapper;
import chh.spring.service.FollowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;

    // 关注作者（增加逻辑删除过滤）
    @Override
    @Transactional
    public void followAuthor(Integer followerId, Integer followedId) {
        // 1. 排除逻辑删除的记录（关键！）
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
                .eq("followed_id", followedId)
                .eq("is_deleted", 0); // 只查未删除的记录

        if (followMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已关注该作者！");
        }

        // 2. 新增关注记录
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowedId(followedId);
        followMapper.insert(follow);

        // 3. 更新统计数
        userMapper.incrFollowCount(followerId);
        userMapper.incrFansCount(followedId);
    }

    // 取消关注作者（强制物理删除，绕过逻辑删除）
    @Override
    @Transactional
    public void unfollowAuthor(Integer followerId, Integer followedId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId).eq("followed_id", followedId);
        followMapper.deletePhysically(followerId, followedId);
        userMapper.decrFollowCount(followerId);
        userMapper.decrFansCount(followedId);
    }

    // 检查是否已关注（增加逻辑删除过滤）
    @Override
    public boolean isFollowed(Integer followerId, Integer followedId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
                .eq("followed_id", followedId)
                .eq("is_deleted", 0); // 只查未删除的记录
        return followMapper.selectCount(wrapper) > 0;
    }

    // 获取关注数和粉丝数
    @Override
    public Map<String, Integer> getFollowAndFansCount(Integer userId) {
        User user = userMapper.selectById(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("followCount", user.getFollowCount());
        result.put("fansCount", user.getFansCount());
        return result;
    }

    @Override
    public List<Integer> getFollowedIds(Integer followerId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
                .eq("is_deleted", 0);
        return followMapper.selectList(wrapper).stream()
                .map(Follow::getFollowedId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getFanIds(Integer followedId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("followed_id", followedId)
                .eq("is_deleted", 0);
        return followMapper.selectList(wrapper).stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
    }
}