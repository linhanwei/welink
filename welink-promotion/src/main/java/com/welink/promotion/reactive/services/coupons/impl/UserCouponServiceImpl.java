package com.welink.promotion.reactive.services.coupons.impl;

import com.mysql.jdbc.Connection;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.promotion.reactive.services.coupons.UserCouponService;
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
 * Created by saarixx on 9/3/15.
 */
@Service
public class UserCouponServiceImpl implements UserCouponService, InitializingBean {

    static Logger logger = LoggerFactory.getLogger(UserCouponServiceImpl.class);

    @Resource
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;

    @Override
    public boolean updateUserCoupon(final UserInteractionRecordsDO userInteractionRecordsDO,
                                    final UserCouponDO userCouponDO) {

        checkNotNull(userInteractionRecordsDO);
        checkNotNull(userCouponDO);

        return transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {

                if (userCouponDO.getId() != null) {
                    // 表示更新
                    if (userCouponDOMapper.updateByPrimaryKey(userCouponDO) != 1) {
                        transactionStatus.setRollbackOnly();
                        logger.error("userCouponDOMapper update error, the input parameters is {}",
                                ToStringBuilder.reflectionToString(userCouponDO, new NoNullFieldStringStyle()));
                        return false;
                    }
                } else {

                    // 表示领取
                    userCouponDO.setActionId(userInteractionRecordsDO.getId());

                    if (userCouponDOMapper.insert(userCouponDO) != 1) {
                        transactionStatus.setRollbackOnly();
                        logger.error("userCouponDOMapper insert error, the input parameters is {}",
                                ToStringBuilder.reflectionToString(userCouponDO, new NoNullFieldStringStyle()));
                        return false;
                    }


                    // 互动更新destination，表示此次互动获得优惠券
                    userInteractionRecordsDO.setDestination(String.valueOf(userCouponDO.getId()));
                    userInteractionRecordsDO.setLastUpdated(new Date());
                    userInteractionRecordsDO.setVersion(2L);
                    if (userInteractionRecordsDOMapper.updateByPrimaryKey(userInteractionRecordsDO) != 1) {
                        transactionStatus.setRollbackOnly();
                        logger.error("userInteractionRecordsDOMapper updateByPrimaryKey error, the input parameters is {}",
                                ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
                        return false;
                    }

                }


                return true;
            }
        });
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("coupon-interactive");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
    }
}
