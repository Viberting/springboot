package chh.spring.service;

import chh.spring.entity.Comment;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
public interface ICommentService extends IService<Comment> {
    public Result getAPageCommentByArticleId(Integer articleId, PageParams pageParams);
    public Comment insert(Comment comment);

}
