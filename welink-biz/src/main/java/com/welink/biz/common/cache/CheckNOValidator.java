package com.welink.biz.common.cache;

import com.welink.commons.commons.BizConstants;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by daniel on 14-9-17.
 */
@Service
public class CheckNOValidator {

    @Resource
    private MemcachedClient memcachedClient;

    public boolean checkNOisValid(String code, String mobile) {
        boolean valid = false;
        String codeCached = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        if (null != code && StringUtils.equals(code, codeCached)) {
            valid = true;
        }
        return valid;
    }
}
