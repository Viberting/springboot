package chh.spring.entity.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDTO {
    private String username;
    private String email;
    private String password;
    private String avatar;
    private String intro;
    private String githubUrl;
    private String bgImage;
    private Integer gender;
    private LocalDate birthday;
}