package chh.spring.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    //页号
    private Long page = 1L;
    //每页行数
    private Long rows = 2L;
    //总记录数
    private Long total;
    //有效性过滤
    private Integer valid;
    
    // 添加搜索条件
    private String username;  // 用户名搜索
    private String email;     // 邮箱搜索
    private LocalDate startDate; // 注册开始日期
    private LocalDate endDate;   // 注册结束日期
}