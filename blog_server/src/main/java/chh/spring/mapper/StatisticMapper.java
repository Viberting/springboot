package chh.spring.mapper;

import chh.spring.entity.Statistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-30
 */
@Mapper
public interface StatisticMapper extends BaseMapper<Statistic> {
    @Select("SELECT * From t_statistic WHERE article_id=#{articleId}")
    public Statistic selectByArticleId(Integer articleId);

}
