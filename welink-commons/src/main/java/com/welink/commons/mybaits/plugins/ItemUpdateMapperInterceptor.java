package com.welink.commons.mybaits.plugins;

import com.google.common.base.Throwables;
import com.google.common.eventbus.AsyncEventBus;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.events.ItemUpdateEvent;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.ibatis.mapping.SqlCommandType.INSERT;
import static org.apache.ibatis.mapping.SqlCommandType.UPDATE;

/**
 * 会拦截到item的update变化
 * <p/>
 * Created by saarixx on 10/12/14.
 */
@Intercepts(value =
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
        })
public class ItemUpdateMapperInterceptor implements Interceptor {

    static Logger logger = LoggerFactory.getLogger(ItemUpdateMapperInterceptor.class);

    public static final String SPECIAL_NAMESPACE = ItemMapper.class.getName();

    private AsyncEventBus asyncEventBus;

    public void setAsyncEventBus(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object object = null;

        long start = System.currentTimeMillis();

        try {
            object = invocation.proceed();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw Throwables.propagate(t);
        }

        long end = System.currentTimeMillis();

        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

            String sqlId = mappedStatement.getId();
            String namespace = sqlId.substring(0, sqlId.lastIndexOf('.'));

            Object parameter = invocation.getArgs()[1];

            if (SPECIAL_NAMESPACE.equals(namespace) && (INSERT == mappedStatement.getSqlCommandType() || UPDATE == mappedStatement.getSqlCommandType())) {
                Executor exe = (Executor) invocation.getTarget();
                String methodName = invocation.getMethod().getName();

                asyncEventBus.post(new ItemUpdateEvent(start, end, mappedStatement.getSqlCommandType()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


        return object;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    @Override
    public void setProperties(Properties properties) {

    }

    @PostConstruct
    public void init() {
        checkNotNull(asyncEventBus);
    }


}
