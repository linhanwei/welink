package com.welink.buy.utils;

import com.welink.commons.utils.NoNullFieldStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 目前来说buy里面所有的result会继承这个
 * <p/>
 * Created by saarixx on 18/9/14.
 */
public abstract class Base {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new NoNullFieldStringStyle());
    }
}
