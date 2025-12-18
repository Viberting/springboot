// controller包下都是类。是后端项目中与前端请求进行交互（传递数据）的类，前端数据只能传给controller包下的某个类
package chh.spring.controller;

import chh.spring.entity.Article;
import chh.spring.service.ArticleService;
import chh.spring.tools.ArticleSearch;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//@RestController表示HelloController是一个控制器，且返回的是json格式的数据
@RestController
@RequestMapping("/article")
public class ArticleController {
    // 注入系统自动创建的Service对象，注意下面的对象名（首字母小写的类名）
    @Autowired
    private ArticleService articleService;

    @PostMapping("/articleSearch")
    public Result articleSearch(@RequestBody ArticleSearch articleSearch) {
        Result result = new Result();
        try{
            result=articleService.articleSearch(articleSearch);
        }catch (Exception e) {
            result.setErrorMessage("获取数据失败！");
            e.printStackTrace();
        }
        return result;
    }

    //主页打开时或从文章返回主页时（要恢复之前的状态）调用
    @PostMapping("/getIndexData1")
    public Result getIndexData1(@RequestBody PageParams pageParams) {
        Result result = new Result();
        try{
            result=articleService.getIndexData(pageParams);
        }catch (Exception e){
            result.setErrorMessage("获取数据失败！");
            e.printStackTrace();
        }
        return result;
    }

    //主页通过分页组件分页查询时调用
    @PostMapping("/getAPageOfArticle")
    public Result getAPageOfArticle(@RequestBody PageParams pageParams) {
        Result result = new Result();
        try {
            result = articleService.getAPageOfArticle(pageParams);
        } catch (Exception e) {
            result.setErrorMessage("获取文章失败！");
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/getIndexData")
    public Result getIndexData(){
        Result result = new Result();
        try{
            result=articleService.getIndexData();
        }catch (Exception e){
            result.setErrorMessage("获取数据失败！");
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping("/deleteById")
    public Result deleteById(Integer id){
        Result result = new Result();
        try{
            articleService.deleteById(id);
        }catch (Exception e){
            result.setErrorMessage("删除失败！");
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/getAPageOfArticleVO")
    public Result getAPageOfArticleVO(@RequestBody PageParams pageParams){
        Result result=new Result();
        try{
            result=articleService.getAPageOfArticleVO(pageParams,"id");
        }catch (Exception e){
            result.setErrorMessage("查询文章失败！");
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/getArticleAndFirstPageCommentByArticleId")
    public Result getArticleAndFirstPageCommentByArticleId(Integer articleId,@RequestBody PageParams pageParams){
        Result result=new Result();
        try{
            result=articleService.getArticleAndFirstPageCommentByArticleId(articleId,pageParams);
        }catch (Exception e){
            result.setErrorMessage("查询文章失败！");
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/getArticleAndFirstPageCommentByArticleId")
    public Result getArticleAndFirstPageCommentByArticleIdGet(@RequestParam Integer articleId,
                                                              @RequestParam(defaultValue = "1") Long page,
                                                              @RequestParam(defaultValue = "10") Long size) {
        Result result = new Result();
        try {
            PageParams pageParams = new PageParams();
            pageParams.setPage(page);
            pageParams.setRows(size);
            result = articleService.getArticleAndFirstPageCommentByArticleId(articleId, pageParams);
        } catch (Exception e) {
            result.setErrorMessage("查询文章失败！");
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/upload")
    public Result upload(MultipartFile  file){
        Result result = new Result();
        try{
            String url = articleService.upload(file);
            result.getMap().put("url",url);//保存返回结果
        }catch (Exception e){
            result.setErrorMessage("上传失败！");
            e.printStackTrace();
        }
        return result;
    }

    //此时它的完整访问路径是： /article/publishArticle
    @RequestMapping("/publishArticle")
    public String publishArticle(String type,@RequestBody Article article){
        try {       //添加和修改文章功能整合在一块，用type参数区分
            if("add".equals(type))
                articleService.publish(article);    //调用Service的一个方法（业务逻辑）
            else if ("edit".equals(type))
                articleService.update(article);
            return "添加成功！";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "添加失败！";
    }

    @RequestMapping("/selectById")
    public Result selectById(Integer id){
        Result result = new Result();
        try{
            Article article=articleService.selectById(id);
            result.getMap().put("article",article);
        }catch (Exception e){
            result.setErrorMessage("查询失败！");
            e.printStackTrace();
        }
        return result;
    }
}
