package chh.spring.entity.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleVO {
    private Integer id; //文章id
    private String title;   //文章标题
    private Date created;   //文章创建时间
    private String categories;  //文章分类
    private Integer hits;   //点击量

    // 新增作者信息字段（关联t_user表）
    private Integer authorId;       // 作者ID
    private String authorName;      // 作者用户名
    private String authorAvatar;    // 作者头像
    private String authorIntro;     // 作者简介
    private Integer authorFollowCount; // 作者关注数
    private Integer authorFansCount;   // 作者粉丝数
    private Boolean isFollowed;     // 当前登录用户是否关注该作者（关键：用于前端按钮状态）
}
