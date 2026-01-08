package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_user_authority") // 用户-权限关联表
public class UserAuthority {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id; // 主键ID
    
    private Integer userId; // 关联用户ID
    private Integer authorityId; // 关联权限ID（默认2=普通用户权限）
}