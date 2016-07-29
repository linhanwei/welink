package com.welink.promotion.reactive.services.points.impl;

import com.mysql.jdbc.Connection;
import com.welink.commons.domain.PointAccountDO;
import com.welink.commons.domain.PointAccountDOExample;
import com.welink.commons.domain.PointRecordDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.persistence.PointAccountDOMapper;
import com.welink.commons.persistence.PointRecordDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.promotion.reactive.services.points.UserPointService;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 交互获取优惠积分
 * <p/>
 * Created by saarixx on 6/3/15.
 */
@Service
public class UserPointServiceImpl implements UserPointService, InitializingBean {

    static Logger logger = LoggerFactory.getLogger(UserPointServiceImpl.class);

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @Resource
    private PointAccountDOMapper pointAccountDOMapper;

    @Resource
    private PointRecordDOMapper pointRecordDOMapper;


    private TransactionTemplate transactionTemplate;

    @Override
    public boolean updateUserPoint(final UserInteractionRecordsDO userInteractionRecordsDO, final PointRecordDO pointRecordDO, final PointAccountDO pointAccountDO) {

        checkNotNull(pointAccountDO);
        checkNotNull(pointAccountDO.getId());
        checkNotNull(pointAccountDO.getUserId());

        return transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {

                // pointAccountDo 只是负责 update
                PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
                pointAccountDOExample.createCriteria() //
                        .andIdEqualTo(pointAccountDO.getId()) //
                        .andStatusEqualTo((byte)1);
                        //.andVersionEqualTo(pointAccountDO.getVersion() - 1);
                if (pointAccountDOMapper.updateByExampleSelective(pointAccountDO, pointAccountDOExample) != 1) {
                    transactionStatus.setRollbackOnly();
                    logger.error("pointAccountDOMapper update error, the input parameters is {}",
                            ToStringBuilder.reflectionToString(pointAccountDO, new NoNullFieldStringStyle()));
                    return false;
                }

                userInteractionRecordsDO.setVersion(1L);
                userInteractionRecordsDO.setDateCreated(new Date());
                if (null == userInteractionRecordsDO.getValue() || userInteractionRecordsDO.getValue() < 1 
                		|| userInteractionRecordsDOMapper.insertSelective(userInteractionRecordsDO) != 1) {
                    transactionStatus.setRollbackOnly();
                    logger.error("userInteractionRecordsDOMapper insert error, the input parameters is {}",
                            ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
                    return false;
                }

                pointRecordDO.setActionId(userInteractionRecordsDO.getId());
                pointRecordDO.setVersion(1L);
                pointRecordDO.setDateCreated(new Date());

                if (null == pointRecordDO.getAmount() || pointRecordDO.getAmount() < 1
                		|| pointRecordDOMapper.insertSelective(pointRecordDO) != 1) {
                    transactionStatus.setRollbackOnly();
                    logger.error("pointRecordDOMapper insert error, the input parameters is {}",
                            ToStringBuilder.reflectionToString(pointRecordDO, new NoNullFieldStringStyle()));
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("point-interactive");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
    }


}
