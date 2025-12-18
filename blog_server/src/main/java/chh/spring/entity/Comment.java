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
    // 新增字段：适配你的表
    private String ip; // 评论者IP
    private String status; // 评论状态（如approved）
}