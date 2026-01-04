package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_authority") // 权限表
public class Authority {
    @TableId(type = IdType.AUTO)
    private Integer id; // 权限ID（1=ROLE_admin，2=ROLE_common）
    
    @TableField("authority")
    private String name; // 权限名称（如"ROLE_common"）
}