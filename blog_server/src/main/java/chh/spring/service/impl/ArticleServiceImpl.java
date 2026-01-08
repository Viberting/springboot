// service包下有若干接口和一个子包，接口名的后缀都是Service，代表后端提供的服务（功能）。子包下是上述接口的实现类
package chh.spring.service.impl;

import chh.spring.entity.User;
import chh.spring.entity.vo.ArticleVO;
import chh.spring.entity.vo.CommentVO;
import chh.spring.entity.Statistic;
import chh.spring.mapper.ArticleMapper;
import chh.spring.entity.Article;
import chh.spring.mapper.CommentMapper;
import chh.spring.mapper.StatisticMapper;
import chh.spring.mapper.UserMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    @Autowired
    private UserMapper userMapper;

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
        // 查询文章基础信息
        Article article = articleMapper.selectById(articleId);
        result.getMap().put("article", article);
        result.getMap().put("article",articleMapper.selectById(articleId));

        // 2. 查询作者信息（核心新增）
        User author = articleMapper.selectAuthorByArticleId(articleId);
        result.getMap().put("author", author);

        // 3. 查询当前登录用户是否关注该作者（需从Security上下文获取当前用户ID）
        Integer currentUserId = getCurrentUserId(); // 需实现：从登录态获取用户ID
        Boolean isFollowed = false;
        if (currentUserId != null && author != null) {
            Integer count = articleMapper.isFollowed(currentUserId, author.getId());
            isFollowed = count > 0;
        }
        result.getMap().put("isFollowed", isFollowed);
        
        // 修复分页查询评论的调用方式
        Page<CommentVO> page = new Page<>(pageParams.getPage(), pageParams.getRows());
        IPage<CommentVO> commentPage = commentMapper.getCommentByArticleId(page, articleId);
        result.getMap().put("comments", commentPage.getRecords());
        
        //文章的点击次数+1
        Statistic statistic = statisticMapper.selectByArticleId(articleId);
        statistic.setHits(statistic.getHits()+1);
        statisticMapper.updateById(statistic);
        return result;
    }

    // 辅助方法：从Security上下文获取当前登录用户ID（需根据你的认证方式调整）
    private Integer getCurrentUserId() {
        try {
            // Spring Security示例：获取当前用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                // 根据用户名查询用户ID（需注入UserMapper）
                User user = userMapper.findByName(userDetails.getUsername());
                return user.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 未登录返回null
    }
    @SneakyThrows
    @Override
    public String upload(MultipartFile file){
        // 添加日志记录
        System.out.println("开始上传文件: " + file.getOriginalFilename());

        final File folder = new File(uploadImagesDir);
        if(!folder.exists()){
            System.out.println("创建上传目录: " + uploadImagesDir);
            boolean created = folder.mkdirs();//创建文件夹
            if (!created) {
                System.out.println("创建上传目录失败: " + uploadImagesDir);
                throw new RuntimeException("创建上传目录失败");
            }
        }

        // 检查目录是否可写
        if (!folder.canWrite()) {
            System.out.println("上传目录不可写: " + uploadImagesDir);
            throw new RuntimeException("上传目录不可写");
        }

        // 获得上传文件的扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            System.out.println("文件名无效: " + originalFilename);
            throw new RuntimeException("文件名无效");
        }

        // 使用原始文件名
        String type = originalFilename.substring(
                originalFilename.lastIndexOf(".")
        );
        String newName = originalFilename; // 直接使用原始文件名

        File destFile = new File(folder, newName);

        // 检查文件是否已存在，如果存在则添加数字后缀
        int counter = 1;
        while (destFile.exists()) {
            counter++;
            newName = originalFilename.substring(0, originalFilename.lastIndexOf(".")) + "_" + counter + type;
            destFile = new File(folder, newName);
            if (counter > 100) { // 防止无限循环
                throw new RuntimeException("无法生成唯一的文件名");
            }
        }

        try {
            file.transferTo(destFile);//保存文件
            String url = "/api/images/" + newName;//文件的相对网址
            System.out.println("文件上传成功: " + url);
            return url;
        } catch (IOException e) {
            System.out.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }



    // 发布文章
    @Transactional  //表示publish方法会被作为一个事务处理，确保文章和统计记录同时添加或均不添加
    public void publish(Article article){
        article.setCreated(new Date());
        // 设置作者ID（如果存在）
        if (article.getAuthorId() == null) {
            // 这里应该从当前登录用户获取，但目前暂时设为默认值
            // 在实际应用中，需要从安全上下文获取当前用户ID
            article.setAuthorId(1); // 临时设置，实际应用中需要从安全上下文获取
        }
        // articleMapper使用从父类BaseMapper继承过来的insert方法，将文章添加到表t_article
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
        // 保留作者信息
        newArticle.setAuthorId(article.getAuthorId());
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

    // 根据作者ID获取文章列表
    @Override
    public Result getArticlesByAuthorId(Integer authorId, PageParams pageParams) {
        // 1. 处理分页参数
        long currentPage = pageParams.getPage() != null ? pageParams.getPage() : 1;
        long pageSize = pageParams.getRows() != null ? pageParams.getRows() : 5;
        // 注意：这里用ArticleVO的分页对象
        IPage<ArticleVO> page = new Page<>(currentPage, pageSize);

        // 2. 调用新增的关联查询方法（返回包含hits的ArticleVO）
        IPage<ArticleVO> articleVOPage = articleMapper.selectArticleWithHitsByAuthorId(page, authorId);

        // 3. 封装结果（和原有逻辑一致，只是数据换成ArticleVO）
        Result result = new Result();
        PageParams resPageParams = new PageParams();
        resPageParams.setPage(currentPage);
        resPageParams.setRows(pageSize);
        resPageParams.setTotal(articleVOPage.getTotal()); // 总条数
        result.setSuccess(true);
        // 这里返回的是ArticleVO列表（包含hits字段）
        result.getMap().put("articles", articleVOPage.getRecords());
        result.getMap().put("pageParams", resPageParams);
        return result;
    }
}