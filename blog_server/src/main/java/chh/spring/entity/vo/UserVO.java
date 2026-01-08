package chh.spring.entity.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserVO {
    private Integer id; // 用户ID
    private String username; // 用户名
    private String email; // 邮箱
    private LocalDate created; // 创建时间
    private String authorityNames; // 拥有的权限名称（如"ROLE_common,ROLE_admin"）
    // 新增：intro字段
    private String intro;
    // 新增：avatar字段
    private String avatar;
    // 新增：是否已互粉（布尔类型）
    private Boolean isFollowed;
}