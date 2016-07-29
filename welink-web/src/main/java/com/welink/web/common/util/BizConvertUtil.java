package com.welink.web.common.util;

import com.welink.commons.commons.BizConstants;

/**
 * Created by daniel on 14-9-18.
 */
public class BizConvertUtil {

    /**
     * cate to type
     *
     * @param cateId
     * @return
     */
    public static int cateToType(long cateId) {

        if (cateId == 100012) {
            return BizConstants.ORDER_TYPE_WATER;
        } else if (cateId == 100011) {
            return BizConstants.ORDER_TYPE_REPAIR;
        } else if (cateId == 100010) {
            return BizConstants.ORDER_TYPE_HOMEMAKING;
        }
        return -1;
    }

}
