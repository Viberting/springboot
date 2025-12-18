package chh.spring.tools;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
//代表查询条件：可以根据文章标题、创建时间上限和下限查询文章
public class ArticleCondition {
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd")//设定日期的格式，方便客户和服务器端数据交互
    private Date startData;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endData;
}
