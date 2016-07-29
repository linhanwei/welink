package com.welink.web.ons;

import com.aliyun.openservices.ons.api.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by saarixx on 12/12/14.
 */
public interface ProduceProcess {

    public void sendMessage(@Nonnull String topic, @Nonnull String tag, @Nullable String key, @Nonnull String body);
}
