package chh.spring.controller;

import chh.spring.entity.User;
import chh.spring.entity.vo.UserVO;
import chh.spring.mapper.FollowMapper;
import chh.spring.mapper.UserMapper;
import chh.spring.service.FollowService;
import chh.spring.service.UserService;
import chh.spring.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/follow")
public class FollowController {
    @Autowired
    private FollowService followService;

    // 新增：注入UserMapper，用于根据用户名查询用户ID
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    // 关注作者
    @PostMapping("/followAuthor")
    public Result followAuthor(@RequestParam Integer authorId) {
        Result result = new Result();
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                result.setErrorMessage("请先登录！");
                return result;
            }
            // 调用Service关注作者
            followService.followAuthor(currentUserId, authorId);
            result.setSuccess(true);
            result.setMsg("关注成功！");
        } catch (Exception e) {
            result.setErrorMessage("关注失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 取消关注作者
    @PostMapping("/unfollowAuthor")
    public Result unfollowAuthor(@RequestParam Integer authorId) {
        Result result = new Result();
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                result.setErrorMessage("请先登录！");
                return result;
            }
            followService.unfollowAuthor(currentUserId, authorId);
            result.setSuccess(true);
            result.setMsg("取消关注成功！");
        } catch (Exception e) {
            result.setErrorMessage("取消关注失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 修复：补充完整的获取当前用户ID逻辑，确保有返回值
    private Integer getCurrentUserId() {
        try {
            // 从Spring Security上下文获取登录用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                // 根据用户名查询用户ID
                User user = userMapper.findByName(userDetails.getUsername());
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 所有分支都有返回值：未登录/异常时返回null
        return null;
    }

    // 返回用户的关注数和粉丝数
    @GetMapping("/getFollowAndFansCount")
    public Result getFollowAndFansCount(@RequestParam Integer userId) {
        Result result = new Result();
        try {
            Map<String, Integer> countMap = followService.getFollowAndFansCount(userId);
            result.setSuccess(true);
            result.getMap().put("followCount", countMap.get("followCount"));
            result.getMap().put("fansCount", countMap.get("fansCount"));
        } catch (Exception e) {
            result.setErrorMessage("获取统计数失败");
            e.printStackTrace();
        }
        return result;
    }

    // 获取关注列表
    @GetMapping("/list")
    public Result getFollowList(@RequestParam Integer userId) {
        Result result = new Result();
        try {
            // 1. 查询当前用户关注的所有作者ID
            List<Integer> followedIds = followService.getFollowedIds(userId);
            // 2. 查询这些作者的信息
            List<User> follows = userService.listByIds(followedIds);
            // 3. 封装为VO（仅返回需要的字段）
            List<UserVO> voList = follows.stream().map(user -> {
                UserVO vo = new UserVO();
                vo.setId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setIntro(user.getIntro());
                vo.setAvatar(user.getAvatar());
                return vo;
            }).collect(Collectors.toList());
            result.setSuccess(true);
            result.getMap().put("follows", voList);
        } catch (Exception e) {
            result.setErrorMessage("获取关注列表失败");
        }
        return result;
    }

    // 获取粉丝列表
    @GetMapping("/fans")
    public Result getFansList(@RequestParam Integer userId) {
        Result result = new Result();
        try {
            // 1. 查询当前用户的所有粉丝ID
            List<Integer> fanIds = followService.getFanIds(userId);
            // 2. 查询粉丝信息，并判断是否已回关
            List<UserVO> voList = fanIds.stream().map(fanId -> {
                User user = userService.getById(fanId);
                UserVO vo = new UserVO();
                vo.setId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setIntro(user.getIntro());
                vo.setAvatar(user.getAvatar());
                // 判断当前用户是否已关注该粉丝（即是否互粉）
                vo.setIsFollowed(followService.isFollowed(userId, fanId));
                return vo;
            }).collect(Collectors.toList());
            result.setSuccess(true);
            result.getMap().put("fans", voList);
        } catch (Exception e) {
            result.setErrorMessage("获取粉丝列表失败");
        }
        return result;
    }

    // 回关粉丝（其实就是关注操作）
    @PostMapping("/followBack")
    public Result followBack(@RequestBody Map<String, Integer> params) {
        Result result = new Result();
        try {
            Integer followerId = params.get("followerId");
            Integer followedId = params.get("followedId");
            followService.followAuthor(followerId, followedId);
            result.setSuccess(true);
            result.setMsg("回关成功");
        } catch (Exception e) {
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }


}