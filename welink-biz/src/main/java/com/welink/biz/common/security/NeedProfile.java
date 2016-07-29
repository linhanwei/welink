package com.welink.biz.common.security;

/**
 * Created by daniel on 15-3-30.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NeedProfile {
    SessionType value() default SessionType.GLANCER;
}
