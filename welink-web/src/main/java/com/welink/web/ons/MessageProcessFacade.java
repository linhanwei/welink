package com.welink.web.ons;

import com.aliyun.openservices.ons.api.*;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.welink.commons.Env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 12/12/14.
 */
@Service("produceProcess")
public class MessageProcessFacade implements ProduceProcess {
	
    static Logger logger = LoggerFactory.getLogger(MessageProcessFacade.class);

    public static final String ACCESS_KEY = "aHg4n4dESlLleVJz";					//生产环境

    public static final String SECRET_KEY = "L2iNoTs1UyRStJgUtkTtqmHUS22Thd";
    
    /*public static final String ACCESS_KEY = "GDheOkyZuLg7VALU";				//开发环境

    public static final String SECRET_KEY = "7sQA8nMHZkB3CNgspOWnpzrl5B7tx0";*/

    public static Map<String, MessageProcessRegister> topicMessageRegisterMap = Maps.newConcurrentMap();


    public void register(@Nonnull MessageProcess messageProcess) {
    	System.out.println("###############################################################");
    	System.out.println("######################Topic:.."+messageProcess.getTopic());
        if (topicMessageRegisterMap.containsKey(messageProcess.getTopic())) {
            //throw new IllegalStateException("duplicate topic process [" + messageProcess.getTopic() + "] ...");
        	return;
        }

        topicMessageRegisterMap.put(messageProcess.getTopic(), new MessageProcessRegister(messageProcess));
    }

    @Override
    public void sendMessage(@Nonnull String topic, @Nonnull String tag, @Nullable String key, @Nonnull String body) {
        Producer producer = checkNotNull(topicMessageRegisterMap.get(topic)).producer;

        Message message = new Message(topic, tag, key, body.getBytes());

        producer.send(message);
    }

    static class MessageProcessRegister {

        private Producer producer;

        private Consumer consumer;

        public MessageProcessRegister(final MessageProcess messageProcess) {
        	System.out.println("-----------------------------------------------------------------------");
        	System.out.println("----AccessKey:"+messageProcess.getAccessKey()
        			+"---SecretKey:"+messageProcess.getSecretKey()+"----Topic:"+messageProcess.getTopic());
            Properties pprops = new Properties();
            pprops.put(PropertyKeyConst.ProducerId, messageProcess.getProducerId());
            /*pprops.put(PropertyKeyConst.AccessKey, ACCESS_KEY);
            pprops.put(PropertyKeyConst.SecretKey, SECRET_KEY);*/
            pprops.put(PropertyKeyConst.AccessKey, messageProcess.getAccessKey());
            pprops.put(PropertyKeyConst.SecretKey, messageProcess.getSecretKey());
            producer = ONSFactory.createProducer(pprops);
            producer.start();

            Properties cprops = new Properties();
            cprops.put(PropertyKeyConst.ConsumerId, messageProcess.getConsumerId());
            /*cprops.put(PropertyKeyConst.AccessKey, ACCESS_KEY);
            cprops.put(PropertyKeyConst.SecretKey, SECRET_KEY);*/
            cprops.put(PropertyKeyConst.AccessKey, messageProcess.getAccessKey());
            cprops.put(PropertyKeyConst.SecretKey, messageProcess.getSecretKey());
            this.consumer = ONSFactory.createConsumer(cprops);
            consumer.subscribe(messageProcess.getTopic(), "*", new MessageListener() {
                @Override
                public Action consume(Message message, ConsumeContext consumeContext) {
                    try {
                        messageProcess.getConsumeProcess().consume(message, consumeContext);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        throw Throwables.propagate(e);
                    }
                    return Action.CommitMessage;
                }
            });
            this.consumer.start();

        }
    }


    @PreDestroy
    public final void close() {
        for (Map.Entry<String, MessageProcessRegister> entry : topicMessageRegisterMap.entrySet()) {
            entry.getValue().producer.shutdown();
            entry.getValue().consumer.shutdown();
        }

        topicMessageRegisterMap.clear();
    }
}
