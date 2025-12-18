package chh.spring.entity.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleVO {
    private Integer id; //文章id
    private String title;   //文章标题
    private Date created;   //文章创建时间
    private String categories;  //文章分类

    private Integer hits;   //点击量
}
