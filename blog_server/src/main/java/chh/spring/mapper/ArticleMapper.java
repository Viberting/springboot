package chh.spring.mapper;

import chh.spring.entity.Article;
import chh.spring.entity.vo.ArticleVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {
    @Select("SELECT t_article.id,title,created,categories,t_statistic.hits "+
    " FROM t_article,t_statistic"+
    " ${ew.customSqlSegment}")
    IPage<ArticleVO> getAPageOfArticleVO(IPage<ArticleVO> page, @Param("ew") Wrapper wrapper);

    //具体的sql见xml包中的ArticleMapper.xml  其sql语句在对应的xml文件中，这里只要指出方法名、参数和返回值类型
    public List<Article> getAPage(
            @Param("offset")Long offset,//offset表示从符合条件的第几条记录开始返回，从0开始计算
            @Param("size")Long size);//返回几条数据

    //分页查询文章
    @Select("SELECT * FROM t_article ${ew.customSqlSegment}")
    IPage<Article> getAPageOfArticle(IPage<Article> page, @Param("ew") Wrapper wrapper);

    //查询文章的方法articleSearch
    @Select("SELECT id,categories,title,created FROM t_article ${ew.customSqlSegment} ")
    IPage<ArticleVO> articleSearch(IPage<ArticleVO> page, @Param("ew") Wrapper wrapper);
}
