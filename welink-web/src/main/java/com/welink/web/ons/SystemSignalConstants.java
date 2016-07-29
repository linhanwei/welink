package com.welink.web.ons;

/**
 * Created by saarixx on 15/12/14.
 */
public class SystemSignalConstants {

    public static final String SYSTEM_SIGNAL_PREFIX = "$$SYSTEM_SIGNAL$$_";

    public static enum Signal {
        SEARCH_ITEM_REFRESH("SEARCH_ITEM_REFRESH", "搜索商品索引重建"),;

        private String code;

        private String desc;

        Signal(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
