package com.welink.web.search;

import com.mysql.jdbc.Connection;
import com.welink.commons.domain.TransCheckDO;
import com.welink.commons.domain.TransCheckDOExample;
import com.welink.commons.persistence.TransCheckDOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by saarixx on 5/12/14.
 */
@Service
public class TransactionCheckService {

    static Logger logger = LoggerFactory.getLogger(TransactionCheckService.class);

    @Resource
    private TransCheckDOMapper transCheckDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;


    public void check() {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("transaction-check");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                TransCheckDOExample tExample = new TransCheckDOExample();
                tExample.createCriteria().andIdIsNotNull();
                //1. 读取
                List<TransCheckDO> transCheckDOList = transCheckDOMapper.selectByExample(tExample);
                try {
                    //2. update
                    if (null != transCheckDOList && transCheckDOList.size() > 0) {

                        long tId = transCheckDOList.get(0).getId();
                        long num = transCheckDOList.get(0).getNum();
                        TransCheckDOExample ttExample = new TransCheckDOExample();
                        ttExample.createCriteria().andIdEqualTo(tId).andNumEqualTo(num);
                        TransCheckDO transCheckDO = new TransCheckDO();
                        Date date = new Date();
                        transCheckDO.setNum(num + 1);
                        transCheckDO.setLastUpdated(new Date());
                        if (transCheckDOMapper.updateByExampleSelective(transCheckDO, ttExample) < 1) {
                            logger.error("update trans check failed. date :" + date);
                            status.setRollbackOnly();
                            return;
                        }
                    } else {
                        TransCheckDO transCheckDO = new TransCheckDO();
                        transCheckDO.setDateCreated(new Date());
                        transCheckDO.setLastUpdated(new Date());
                        transCheckDO.setNum(Long.MIN_VALUE);
                        transCheckDOMapper.insert(transCheckDO);
                    }
                } catch (Exception e) {
                    logger.error("update trans check failed. date :" + new Date() + ", exp:" + e.getMessage(), e);
                    status.setRollbackOnly();
                    return;
                }
            }
        });
    }

}