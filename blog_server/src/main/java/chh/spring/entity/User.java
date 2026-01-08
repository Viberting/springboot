package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
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

    private Integer valid;
    
    private Integer followCount;
    private Integer fansCount;
    
    // 新增的个人中心字段（暂时标记为不存在，避免SQL错误）
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