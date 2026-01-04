// model包下有若干类，一个类代表一个数据库表的结构，类的属性代表数据库表的一个字段
package chh.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor  //lombok的注解，可以自动创建
@AllArgsConstructor //get、set方法、构造方法等
@TableName("t_article") //对应的数据库表名
public class Article {
    @TableId(type = IdType.AUTO)    //id字段为自动增长
    private Integer id; //文章id
    private String title;   //文章标题
    private String content; //文章内容
    private Date created;   //文章创建时间
    private Date modified;  //文章修改时间
    private String categories;  //文章分类
    private String tags;    //文章标签
    private Boolean allowComment;   //是否允许评论,默认为true
    private String thumbnail;   //文章缩略图
    @TableField(exist = false)   // 暂时标记为不存在，避免查询时出错
    private Integer authorId;   //文章作者ID
}
