package com.welink.commons.persistence;

import com.mysql.jdbc.Connection;
import com.welink.commons.domain.TransCheckDO;
import com.welink.commons.domain.TransCheckDOExample;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.notNullValue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("test")
public class TransCheckDOMapperTest {

    static Logger logger = LoggerFactory.getLogger(TransCheckDOMapperTest.class);

    @Resource
    private TransCheckDOMapper transCheckDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Before
    public void init() throws Exception {
        MatcherAssert.assertThat(transactionManager, notNullValue());
    }

    @Test
    public void test_accuracy() throws InterruptedException {

        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setTimeout(5);
        transactionTemplate.setName("transaction-check");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);

        for (int i = 0; i < 1000; i++)
            executorService.execute(new Runnable() {
                @Override
                public void run() {

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
                                    if (transCheckDOMapper.updateByExampleSelective(transCheckDO, ttExample) < 0) {
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
            });

        Thread.sleep(10000000);
    }

}