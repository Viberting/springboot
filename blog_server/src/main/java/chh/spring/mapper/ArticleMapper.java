package chh.spring.mapper;

import chh.spring.entity.Article;
import chh.spring.entity.User;
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

    // 新增：根据作者ID关联查询文章+阅读量
    @Select("SELECT a.id, a.title, a.created, a.categories, s.hits " +
            "FROM t_article a " +
            "LEFT JOIN t_statistic s ON a.id = s.article_id " + // 关联阅读量表
            "WHERE a.author_id = #{authorId} " + // 按作者ID筛选
            "ORDER BY a.created DESC")
    IPage<ArticleVO> selectArticleWithHitsByAuthorId(IPage<ArticleVO> page, @Param("authorId") Integer authorId);

    // 新增：根据文章ID查询作者信息
    @Select("SELECT u.* FROM t_article a LEFT JOIN t_user u ON a.author_id = u.id WHERE a.id = #{articleId}")
    User selectAuthorByArticleId(@Param("articleId") Integer articleId);

    // 新增：查询当前用户是否关注某作者（用于判断按钮状态）
    @Select("SELECT COUNT(*) FROM t_follow WHERE follower_id = #{userId} AND followed_id = #{authorId}")
    Integer isFollowed(@Param("userId") Integer userId, @Param("authorId") Integer authorId);
}
