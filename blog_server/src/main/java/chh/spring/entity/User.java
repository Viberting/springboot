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

    private Boolean valid;

    // 新增的个人中心字段
    private String avatar;

    private String intro;

    private String githubUrl;

    private String bgImage;

    private Integer gender;

    private LocalDate birthday;

    // ========== 新增：关注数、粉丝数字段 ==========
    private Integer followCount; // 对应t_user表的follow_count字段
    private Integer fansCount;   // 对应t_user表的fans_count字段
}