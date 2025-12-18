package chh.spring.service.impl;

import chh.spring.mapper.ArticleMapper;
import chh.spring.entity.Article;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ArticleServiceImplArticalServiceMockitoTest {
    @Mock//一般是被测对象依赖的对象
    private ArticleMapper articleMapper;//注入其他用于测试的类

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Test
    void publish() {
        Article article = new Article();
        article.setTitle("新闻标题");
        article.setContent("新闻内容");
        article.setTags("新闻标签");

        articleService.publish(article);

        Mockito.verify(articleMapper,times(1)).insert(article);
        assertEquals("新闻标签", article.getTags());

    }

}