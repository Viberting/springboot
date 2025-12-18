package chh.spring.service.impl;

import chh.spring.entity.vo.ArticleVO;
import chh.spring.mapper.ArticleMapper;
import chh.spring.entity.Article;
import chh.spring.service.ArticleService;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest//SpringBoot注解，提供SpringBoot项目测试中使用的应用程序上下文环境
class ArticleServiceImplOnlyUnit5Test {//测试类
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleMapper articleMapper;
    private static Article article;

    @BeforeAll//JUnit5注解，在所有单元测试之前执行一次
    static void testBeforeAll(){
        article=new Article();
        article.setTitle("新闻标题");
        article.setContent("新闻内容");
        article.setTags("新闻标签");
    }

    @Test
    public void getAPageOfArticleVO(){
        PageParams pageParams = new PageParams();
        pageParams.setPage(1L);
        pageParams.setRows(2L);
        Result result = articleService.getAPageOfArticleVO(pageParams, "");

        List<ArticleVO> records = (List<ArticleVO>) result.getMap().get("articleVOs");
        assertEquals(12,records.get(0).getId());//判断结果是否正确
        assertEquals(11,records.get(1).getId());
    }


}
