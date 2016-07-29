package com.welink.web.ons;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 12/12/14.
 */
public class MessageProcess {
	private String accessKey;
    private String secretKey;
    
    private String topic;
    private String producerId;
    private String consumerId;
    private ConsumeProcess consumeProcess;

    private MessageProcess(String accessKey, String secretKey, String topic, String producerId, String consumerId, ConsumeProcess consumeProcess) {
    	this.accessKey = checkNotNull(accessKey);
    	this.secretKey = checkNotNull(secretKey);
    	
    	this.topic = checkNotNull(topic);
        this.producerId = checkNotNull(producerId);
        this.consumerId = checkNotNull(consumerId);
        this.consumeProcess = checkNotNull(consumeProcess);
    }

    public String getAccessKey() {
		return accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getTopic() {
        return topic;
    }

    public String getProducerId() {
        return producerId;
    }

    public String getConsumerId() {
        return consumerId;
    }

	public ConsumeProcess getConsumeProcess() {
        return consumeProcess;
    }

    public static MessageProcessBuilder newBuilder() {
        return new MessageProcessBuilder();
    }

    public static class MessageProcessBuilder {
    	private String accessKey;
        private String secretKey;
    	
        private String topic;
        private String producerId;
        private String consumerId;
        private ConsumeProcess consumeProcess;
        
        public MessageProcessBuilder setAccessKey(String accessKey) {
            checkArgument(StringUtils.isNotBlank(accessKey));
            this.accessKey = accessKey;
            return this;
        }
        
        public MessageProcessBuilder setSecretKey(String secretKey) {
            checkArgument(StringUtils.isNotBlank(secretKey));
            this.secretKey = secretKey;
            return this;
        }

        public MessageProcessBuilder setTopic(String topic) {
            checkArgument(StringUtils.isNotBlank(topic));
            this.topic = topic;
            return this;
        }

        public MessageProcessBuilder setProducerId(String producerId) {
            checkArgument(StringUtils.isNotBlank(producerId));
            this.producerId = producerId;
            return this;
        }

        public MessageProcessBuilder setConsumerId(String consumerId) {
            checkArgument(StringUtils.isNotBlank(consumerId));
            this.consumerId = consumerId;
            return this;
        }

        public MessageProcessBuilder setConsumeProcess(ConsumeProcess consumeProcess) {
            this.consumeProcess = checkNotNull(consumeProcess);
            return this;
        }

        public MessageProcess build() {
            return new MessageProcess(accessKey, secretKey, topic, producerId, consumerId, consumeProcess);
        }
    }
}
