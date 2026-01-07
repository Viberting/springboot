package chh.spring.tools;

import lombok.Data;

/**
 * 评论搜索条件类
 */
@Data
public class CommentSearch {
    private String content;  // 评论内容搜索关键词
    private Integer articleId;  // 文章ID
    private String author;  // 评论作者
    private Integer status;  // 评论状态（改为Integer，匹配前端）
    private String startDate;  // 开始日期
    private String endDate;  // 结束日期
    private PageParams pageParams;  // 分页参数
}