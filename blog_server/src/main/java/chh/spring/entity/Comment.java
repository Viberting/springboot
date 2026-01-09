package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("t_comment") // 对应你的评论表名
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id; // 评论ID
    private Integer articleId; // 关联文章ID
    private String content; // 评论内容
    private Date created; // 评论时间
    private String author; // 评论作者
    private Integer isDeleted; // 逻辑删除标识（0=未删）
    // 基础扩展字段
    private String ip; // 评论者IP
    private String status; // 评论状态（如approved）
    private Integer userId; // 关联用户ID（保留该字段）

    // 评论回复相关字段
    private Integer parentId; // 父评论ID，如果是顶级评论则为null
    private Integer rootId; // 根评论ID，用于快速查询同一评论树的全部评论
    private String replyTo; // 被回复的用户名，用于显示"回复@用户名"
}