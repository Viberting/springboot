package chh.spring.service.impl;

import chh.spring.entity.Comment;
import chh.spring.mapper.CommentMapper;
import chh.spring.service.ICommentService;
import chh.spring.tools.PageParams;
import chh.spring.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    private CommentMapper commentMapper;

    public Comment insert(Comment comment){//返回添加后的评论，用于客户端展示
        commentMapper.insert(comment);//添加数据后，comment对象会对应新增加的记录（具有了id等属性值）
        return comment;
    }

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public Result getAPageCommentByArticleId(Integer articleId, PageParams pageParams){
        //查询条件构造器QueryWrapper
        QueryWrapper wrapper=new QueryWrapper<>();//Java代码方式设置魏查询条件
        wrapper.eq("article_id",articleId);//eq表示相等，字段名是表的字段名，不是实体类的属性名
        wrapper.orderBy(true, false, "id");//排序，false表示降序
        //创建分页对象（1表示第一页；2表示每页大小为2）
        Page<Comment> page=new Page<>(pageParams.getPage(),pageParams.getRows());
        Page<Comment> apage=commentMapper.selectPage(page,wrapper);//按条件进行分页查询
        Result result=new Result();
        result.getMap().put("comments",apage.getRecords());
        return result;
    }

}

