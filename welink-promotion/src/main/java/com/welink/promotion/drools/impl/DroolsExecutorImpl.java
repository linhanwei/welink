package com.welink.promotion.drools.impl;

import com.google.common.base.Preconditions;
import com.welink.promotion.drools.DroolsExecutor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kie.api.KieServices;
import org.kie.api.event.rule.*;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * Created by saarixx on 8/3/15.
 */
@Service
public class DroolsExecutorImpl implements DroolsExecutor, InitializingBean {

    static Logger logger = LoggerFactory.getLogger(DroolsExecutor.class);

    private KieContainer kContainer;

    @Override
    public void execute(Object... objects) {
        KieSession kSession = kContainer.newKieSession("promotion-rules");

        Preconditions.checkNotNull(kSession);

        try {
            for (Object object : objects) {
                kSession.insert(object);
            }

            kSession.addEventListener(new DebugRuleRuntimeEventListener());
            kSession.addEventListener(new DefaultAgendaEventListener() {

                @Override
                public void matchCancelled(MatchCancelledEvent event) {
                    super.matchCancelled(event);
                    logger.debug("drools matchCancelled event --> {}", ToStringBuilder.reflectionToString(event));
                }

                @Override
                public void matchCreated(MatchCreatedEvent event) {
                    super.matchCreated(event);
                    logger.debug("drools matchCreated event --> {}", ToStringBuilder.reflectionToString(event));
                }

                public void afterMatchFired(AfterMatchFiredEvent event) {
                    super.afterMatchFired(event);
                    logger.debug("drools afterMatchFired event --> {}", ToStringBuilder.reflectionToString(event));
                }
            });

            kSession.fireAllRules();
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        } finally {
            kSession.dispose();
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        KieServices ks = KieServices.Factory.get();
        kContainer = ks.getKieClasspathContainer();
    }
}
