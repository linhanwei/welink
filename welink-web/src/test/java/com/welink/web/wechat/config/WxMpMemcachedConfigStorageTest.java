package com.welink.web.wechat.config;

import com.daniel.weixin.mp.bean.WxMpXmlMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class WxMpMemcachedConfigStorageTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testXml() {
        String xml = "<xml><ToUserName><![CDATA[gh_0f80202fae70]]></ToUserName>\n" +
                "<FromUserName><![CDATA[oF6JLs90ORQKxbSF5nvvS3_z7E00]]></FromUserName>\n" +
                "<CreateTime>1419569490</CreateTime>\n" +
                "<MsgType><![CDATA[text]]></MsgType>\n" +
                "<Content><![CDATA[哈哈]]></Content>\n" +
                "<MsgId>6097004534151336818</MsgId>\n" +
                "</xml>";
        WxMpXmlMessage wxMpXmlMessage = WxMpXmlMessage.fromXml(xml);
        assertThat(wxMpXmlMessage, notNullValue());
    }
}