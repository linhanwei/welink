package com.welink.web.wechat.service;

import com.daniel.weixin.common.model.response.WxMenu;
import com.daniel.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by saarixx on 27/12/14.
 */
@Service
public class WxMenuService {

    @Resource
    private WxMpService wxMpService;

    @PostConstruct
    public void init() throws Exception {
        WxMenu wxMenu = wxMpService.menuGet();
    }
}
