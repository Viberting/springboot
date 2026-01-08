package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("t_follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followerId;
    private Integer followedId;

    // 1. 将@TableField移到字段上
    @TableField(fill = FieldFill.INSERT) // 仅在插入时自动填充
    private Date created;

    @TableLogic
    private Integer isDeleted;
}