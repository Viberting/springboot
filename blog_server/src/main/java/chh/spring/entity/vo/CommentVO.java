package chh.spring.entity.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CommentVO {
    private Integer id;
    private Integer articleId;
    private String articleTitle; // 关联文章标题
    private String content;
    private Date created;
    private String author;
    // 新增字段（原代码保留）
    private String ip;
    private String status;
    private String userAvatar; // 用户头像（保留该字段）

    // 评论回复相关字段（新增）
    private Integer parentId; // 父评论ID
    private Integer rootId; // 根评论ID
    private String replyTo; // 被回复的用户名

    // 评论树结构字段（新增）
    private List<CommentVO> children; // 子评论列表
}