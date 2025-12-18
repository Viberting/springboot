// service代表业务逻辑，其一个方法，表示一个业务（功能）
// service接口的实现类必须由程序员编写，不会自动生成
package chh.spring.service;

import chh.spring.entity.Article;
import chh.spring.tools.ArticleSearch;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {
    public void deleteById(Integer id);
    public void update(Article article);
    public Article selectById(Integer id);
    // 获取文章列表分页数据
    public Result getAPageOfArticleVO(PageParams pageParams,String type);
    
    public String upload(MultipartFile file);

    // 查一篇文章及其评论
    public Result getArticleAndFirstPageCommentByArticleId(Integer articleId, PageParams pageParams);

    // 发布文章
    public void publish(Article article);

    // 获取首页数据
    public Result getIndexData();

    public Result getIndexData(PageParams pageParams);

    public Result getAPageOfArticle(PageParams pageParams);

    public Result articleSearch(ArticleSearch articleSearch);

    }