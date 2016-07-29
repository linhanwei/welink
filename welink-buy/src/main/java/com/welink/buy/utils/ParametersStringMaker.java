package com.welink.buy.utils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * Created by saarixx on 27/11/14.
 */
public final class ParametersStringMaker {

    public static final String NON_PARAMETER = "$$NON_PARAMETER$$";

    public static final String SEPARATOR = "$$";

    public static String parametersMake(Object... objects) {
        if (objects == null) {
            return NON_PARAMETER;
        }

        return StringUtils.join(Collections2.transform(Lists.newArrayList(objects), new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(Object input) {
                if (input == null) {
                    return "==null==";
                } else {
                    return String.valueOf(input);
                }
            }
        }), SEPARATOR);
    }
}
