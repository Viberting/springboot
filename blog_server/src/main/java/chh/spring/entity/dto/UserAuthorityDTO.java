package chh.spring.entity.dto;

import lombok.Getter;
import lombok.Setter;
import chh.spring.entity.User;

import java.util.List;

/**
 * 专门接收前端提交的用户+权限数据，解决LinkedHashMap强转User报错
 */
@Getter
@Setter
public class UserAuthorityDTO {
    // 接收前端的user对象
    private User user;
    // 接收前端的权限id数组
    private List<Integer> authorityIds;
}