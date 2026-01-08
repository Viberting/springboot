package chh.spring.service;

import java.util.List;
import java.util.Map;

public interface FollowService {
    // 关注作者
    void followAuthor(Integer followerId, Integer followedId);

    // 取消关注作者
    void unfollowAuthor(Integer followerId, Integer followedId);

    // 检查是否已关注（方法签名：返回boolean，参数为两个Integer）
    boolean isFollowed(Integer followerId, Integer followedId);

    // 获取关注数和粉丝数（方法签名：返回Map，参数为Integer）
    Map<String, Integer> getFollowAndFansCount(Integer userId);

    // 新增：获取关注的作者ID列表
    List<Integer> getFollowedIds(Integer followerId);

    // 新增：获取粉丝ID列表
    List<Integer> getFanIds(Integer followedId);

}