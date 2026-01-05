package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("t_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String email;
    private LocalDate created;
    // ✅ 核心：数据库是tinyint(1)，这里用Integer，不要用Boolean，避免类型转换失败
    private Integer valid;

    // User.java中这些字段要标记为不存在（因为表中没有）
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String intro;
    @TableField(exist = false)
    private String githubUrl;
    @TableField(exist = false)
    private String bgImage;
    @TableField(exist = false)
    private Integer gender;
    @TableField(exist = false)
    private LocalDate birthday;
}