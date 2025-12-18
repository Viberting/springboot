// service包下有若干接口和一个子包，接口名的后缀都是Service，代表后端提供的服务（功能）。子包下是上述接口的实现类
package chh.spring.service.impl;

import chh.spring.entity.vo.ArticleVO;
import chh.spring.entity.Statistic;
import chh.spring.mapper.ArticleMapper;
import chh.spring.entity.Article;
import chh.spring.mapper.CommentMapper;
import chh.spring.mapper.StatisticMapper;
import chh.spring.service.ArticleService;
import chh.spring.tools.ArticleSearch;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Wrapper;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional  //表示该Service支持数据库事务
public class ArticleServiceImpl implements ArticleService {
    @Value("${uploadImagesDir}")
    private String uploadImagesDir;//注入application.yml中的uploadImagesDir属性

    // 注入系统自动创建的ArticleMapper对象
    // 注意：被注入的对象名，一般都是和类名基本一致，除了首字母小写。如上面注入的对象名是：articleMapper
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired//注入CommentMapper，使得ArticleServiceImpl可以操作评论表
    private CommentMapper commentMapper;

    @Autowired
    private StatisticMapper statisticMapper;

    //分页查询文章
    public Result getAPageOfArticle(PageParams pageParams){
        QueryWrapper<ArticleVO> wrapper = new QueryWrapper();
        wrapper.orderBy(true,false,"t_article.id");

        Page<Article> page = new Page<Article>(pageParams.getPage(),pageParams.getRows());
        IPage<Article> aPage=articleMapper.getAPageOfArticle(page, wrapper);
        Result result = new Result();
        pageParams.setTotal(aPage.getTotal());
        result.getMap().put("articles",aPage.getRecords());
        result.getMap().put("pageParams",pageParams);
        return result;
    }

    public Result getIndexData(PageParams pageParams){
        //查文章分页
        Result result = getAPageOfArticle(pageParams);

        //查点击量排名前十文章
        PageParams pageParams1=new PageParams();
        pageParams1.setPage(1L);
        pageParams1.setRows(10L);
        Result result1=getAPageOfArticleVO(pageParams1,"hits");
        result.getMap().put("articleVOs",result1.getMap().get("articleVOs"));
        return result;
    }

    public Result getIndexData(){
        Result result = new Result();
        List<Article> articles = articleMapper.getAPage(0L, 5L);
        result.getMap().put("articles",articles);

        PageParams pageParams=new PageParams();
        pageParams.setPage(1L);
        pageParams.setRows(10L);
        Result result1=getAPageOfArticleVO(pageParams,"hits");
        result.getMap().put("articleVOs",result1.getMap().get("articleVOs"));
        return result;
    }
    public Result getAPageOfArticleVO(PageParams pageParams,String type){
        QueryWrapper<ArticleVO> wrapper = new QueryWrapper<>();

        if ("id".equals(type))
            wrapper.orderBy(true,false,"t_article.id");//排序，false表示降序
        else if ("hits".equals(type))
            wrapper.orderBy(true,false,"t_statistic.hits");//按点击量降序排列

        //apply相当于：where t_article.id=t_statistic.article_id
        wrapper.apply("t_article.id=t_statistic.article_id");
        String s=wrapper.getCustomSqlSegment();
        //创建分页对象（1表示第一页 2表示每页大小为2）
        Page<ArticleVO> page = new Page<ArticleVO>(pageParams.getPage(),pageParams.getRows());
        IPage<ArticleVO> aPage = articleMapper.getAPageOfArticleVO( page, wrapper);
        Result result = new Result();
        pageParams.setTotal(aPage.getTotal());
        //aPage中存有符合条件的记录总数  把分页信息返回到客户端
        result.getMap().put("articleVOs",aPage.getRecords());
        result.getMap().put("pageParams",pageParams);
        return result;
    }

    public Result getArticleAndFirstPageCommentByArticleId(Integer articleId, PageParams pageParams){
        Result result = new Result();
        result.getMap().put("article",articleMapper.selectById(articleId));
        result.getMap().put("comments"
                ,commentMapper.getAPageCommentByArticleId(
                        articleId, Math.toIntExact((pageParams.getPage() - 1) * pageParams.getRows()), Math.toIntExact(pageParams.getRows())));
        //文章的点击次数+1
        Statistic statistic = statisticMapper.selectByArticleId(articleId);
        statistic.setHits(statistic.getHits()+1);
        statisticMapper.updateById(statistic);
        return result;
    }
    @SneakyThrows
    @Override
    public String upload(MultipartFile file){//固定套路，MultipartFile file中存放了上传的文件
        final File folder = new File(uploadImagesDir);
        if(!folder.exists()){
            folder.mkdirs();//创建文件夹
        }
        //获得上传文件的扩展名
        String type = file.getOriginalFilename().substring(
                file.getOriginalFilename().lastIndexOf(".")
        );
        //为文件随机取名
        final String newName = UUID.randomUUID() + type;
        file.transferTo(new File(folder, newName));//保存文件
        String url = "/api/images/" + newName;//文件的相对网址
        return url;
    }

    // 发布文章
    @Transactional  //表示publish方法会被作为一个事务处理，确保文章和统计记录同时添加或均不添加
    public void publish(Article article){
        article.setCreated(new Date());
        // articleMapper使用从父类BaseMapper继承过来的insert方法，将文章添加到表t_articl
        articleMapper.insert(article);
        //添加统计记录
        Statistic statistic = new Statistic();
        statistic.setArticleId(article.getId());
        statistic.setHits(0);
        statistic.setCommentsNum(0);
        statisticMapper.insert(statistic);
    }

    public void update(Article article){
        Article newArticle = articleMapper.selectById(article.getId());
        newArticle.setModified(new Date());
        newArticle.setTitle(article.getTitle());
        newArticle.setContent(article.getContent());
        newArticle.setTags(article.getTags());
        newArticle.setThumbnail(article.getThumbnail());
        //articleMapper使用从父类BaseMapper继承过来的insert方法，将文章添加到表t_article
        articleMapper.updateById(newArticle);
    }

    public void deleteById(Integer id){
        //此处未删除评论及文章统计数据
        articleMapper.deleteById(id);
    }

    public Article selectById(Integer id){
        return articleMapper.selectById(id);
    }

    //查询文章的方法
    public Result articleSearch(ArticleSearch articleSearch){
        QueryWrapper<ArticleVO> wrapper = new QueryWrapper<>();
        wrapper.orderBy(true,false,"id");

        //设置查询条件，条件成立才对title进行条件查询(ge大于等于；le小于等于)
        wrapper.like(articleSearch.getArticleCondition().getTitle()!="","title",
                articleSearch.getArticleCondition().getTitle());
        wrapper.ge(articleSearch.getArticleCondition().getStartData()!=null,"created",
                articleSearch.getArticleCondition().getStartData());
        wrapper.le(articleSearch.getArticleCondition().getEndData()!=null,"created",
                articleSearch.getArticleCondition().getEndData());

        Page<ArticleVO> page = new Page<>(articleSearch.getPageParams().getPage(),
                articleSearch.getPageParams().getRows());
        IPage<ArticleVO> aPage = articleMapper.articleSearch(page, wrapper);
        Result result = new Result();
        articleSearch.getPageParams().setTotal(aPage.getTotal());
        result.getMap().put("articleVOs",aPage.getRecords());
        result.getMap().put("pageParams",articleSearch.getPageParams());
        return result;
    }
}
