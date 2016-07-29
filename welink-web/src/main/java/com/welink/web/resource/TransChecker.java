package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.commons.domain.TransCheckDO;
import com.welink.commons.domain.TransCheckDOExample;
import com.welink.commons.persistence.TransCheckDOMapper;
import com.welink.web.search.TransactionCheckService;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daniel on 14-11-25.
 */
@RestController
public class TransChecker {

    @Resource
    private TransCheckDOMapper transCheckDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private TransactionCheckService transactionCheckService;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private static org.slf4j.Logger log = LoggerFactory.getLogger(TransChecker.class);

    @RequestMapping(value = {"/api/m/1.0/transChecker.json", "/api/m/1.0/transChecker.htm", "/api/h/1.0/transChecker.json", "/api/h/1.0/transChecker.htm"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);

        TransCheckDOExample tExample = new TransCheckDOExample();
        tExample.createCriteria().andIdIsNotNull();
        //1. 读取
        List<TransCheckDO> transCheckDOList = null;
        try {
            transCheckDOList = transCheckDOMapper.selectByExample(tExample);
        } catch (Exception e) {
            log.error("update trans check failed. date :" + new Date());
            response.setStatus(500);
            return JSON.toJSONString("-1");
        }
        TransactionStatus transactionStatus = transactionManager.getTransaction(def);
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
                    log.error("update trans check failed. date :" + date);
                    response.setStatus(500);
                    transactionManager.rollback(transactionStatus);
                    return JSON.toJSONString("-1");
                }
            } else {
                TransCheckDO transCheckDO = new TransCheckDO();
                transCheckDO.setDateCreated(new Date());
                transCheckDO.setLastUpdated(new Date());
                transCheckDO.setNum(Long.MIN_VALUE);
                transCheckDOMapper.insert(transCheckDO);
            }
        } catch (Exception e) {
            response.setStatus(500);
            log.error("update trans check failed. date :" + new Date() + ", exp:" + e.getMessage(), e);
            transactionManager.rollback(transactionStatus);
            return JSON.toJSONString("-1");
        }
        try {
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            response.setStatus(500);
            log.error("update trans check failed. date :" + new Date() + ", exp:" + e);
            transactionManager.rollback(transactionStatus);
            return JSON.toJSONString("-1");
        }

        log.info("---------------------- begin ---------------------- " + Thread.currentThread().getId());
        for (int i = 0; i < 500000; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    transactionCheckService.check();
                }
            });
        }
        log.info("---------------------- end ---------------------- " + Thread.currentThread().getId());

        response.setStatus(200);
        return JSON.toJSONString("200");
    }
}
