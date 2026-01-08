package chh.spring.config.security;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component // 必须交给Spring管理
public class MyMetaObjectHandler implements MetaObjectHandler {
    // 插入时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充created字段为当前时间
        this.strictInsertFill(metaObject, "created", Date.class, new Date());
    }

    // 更新时自动填充（如需）
    @Override
    public void updateFill(MetaObject metaObject) {
        // 可添加更新时间的自动填充逻辑
    }
}