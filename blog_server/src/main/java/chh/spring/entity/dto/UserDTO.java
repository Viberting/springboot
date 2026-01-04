package chh.spring.entity.dto;

import chh.spring.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class UserDTO {
    //为了安全，未加入密码属性
    private Integer id;
    private String username;
    private String password; // 添加密码字段，用于新增和编辑用户
    private String email;
    private Boolean valid;
    private List<String> authorities;//权限名称列表
    private List<Integer> authorityIds;//权限ID列表（用于新增/编辑用户）
    
    //将DTO对象转为实体类对象
    public static void dtoToEntity(UserDTO dto, User entity) {
        //复制同名的属性值
        BeanUtils.copyProperties(dto,entity);
    }
    
    //将实体类对象转换为DTO对象
    public static UserDTO entityToDto(User entity) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity,dto);
        return dto;
    }
    
    public List<String> getAuthorities() {
        return authorities;
    }
    
    public List<Integer> getAuthorityIds() {
        return authorityIds;
    }
}