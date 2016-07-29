package com.welink.biz.service;

import com.welink.biz.common.MSG.HttpRequest;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 14-10-20.
 */
public class NotifyAdminService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(NotifyAdminService.class);

    private String url;

    /**
     * 通知给CRM后台
     *
     * @param communityId
     * @return
     */
    public String notifyAdmin(long communityId) {
        String param = "communityId=" + communityId;
        String result = HttpRequest.sendGet(url, param);
        return result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
