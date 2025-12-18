package chh.spring.entity.vo;

import lombok.Data;
import java.util.Date;

@Data
public class CommentVO {
    private Integer id;
    private Integer articleId;
    private String articleTitle; // 关联文章标题
    private String content;
    private Date created;
    private String author;
    // 新增字段
    private String ip;
    private String status;
}