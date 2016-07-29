package com.welink.web.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.digester.SetTopRule;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.sql.visitor.functions.Now;
import com.alibaba.fastjson.JSON;
import com.daniel.weixin.common.exception.WxErrorException;
import com.daniel.weixin.common.session.WxSessionManager;
import com.daniel.weixin.common.util.WxConsts;
import com.daniel.weixin.mp.api.WxMpConfigStorage;
import com.daniel.weixin.mp.api.WxMpMessageHandler;
import com.daniel.weixin.mp.api.WxMpMessageRouter;
import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.WxMpCustomMessage;
import com.daniel.weixin.mp.bean.WxMpXmlMessage;
import com.daniel.weixin.mp.bean.WxMpXmlOutMessage;
import com.daniel.weixin.mp.bean.WxMpXmlOutNewsMessage.Item;
import com.daniel.weixin.mp.bean.WxMpXmlOutTextMessage;
import com.daniel.weixin.mp.bean.WxMpXmlOutTransferCustomerServiceMessage;
import com.daniel.weixin.mp.bean.WxMpCustomMessage.WxArticle;
import com.daniel.weixin.mp.bean.outxmlbuilder.TransferCustomerServiceBuilder;
import com.daniel.weixin.mp.util.xml.XStreamTransformer;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.thoughtworks.xstream.XStream;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.ParameterUtil;
import com.welink.web.wechat.handler.EmployeeTradeQuery;

/**
 * Created by saarixx on 25/12/14.
 */
@RestController
public class WeChatNotification {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WeChatNotification.class);

    @Resource
    private WxMpService wxMpService;

    @Resource
    private WxMpConfigStorage wxMpConfigStorage;

    private WxMpMessageRouter wxMpMessageRouter;
    
    @Resource
    private Env env;

    @Resource
    private EmployeeTradeQuery employeeTradeQuery;

    @RequestMapping(value = {"/api/m/1.0/weChatNotification.json", "/api/m/1.0/weChatNotification.htm", "/api/h/1.0/weChatNotification.json", "/api/h/1.0/weChatNotification.htm"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String mpTag = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        if (env.isProd()) {
        	mpTag = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        } else {
        	mpTag = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        }

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String signature = request.getParameter("signature");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");

        if (StringUtils.isNotBlank(mpTag)) {
            wxMpConfigStorage.setAesKey(mpTag, false);
            wxMpConfigStorage.setAppId(mpTag, false);
            wxMpConfigStorage.setExpiresTime(mpTag, false);
            wxMpConfigStorage.setSecret(mpTag, false);
            wxMpConfigStorage.setToken(mpTag, false);
        }

//        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
//            // 消息签名不正确，说明不是公众平台发过来的消息
//            response.getWriter().println("illegal request ...");
//            return null;
//        }

        String echostr = request.getParameter("echostr");
        if (StringUtils.isNotBlank(echostr)) {
            // 说明是一个仅仅用来验证的请求，回显echostr
            response.getWriter().println(echostr);
            return null;
        }

        String encryptType = StringUtils.isBlank(request.getParameter("encrypt_type")) ?
                "raw" : request.getParameter("encrypt_type");

        if ("raw".equals(encryptType)) {
            // 明文传输的消息
        	try {
        		WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
        		WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
        		response.getWriter().write(outMessage.toXml());
			} catch (Exception e) {
				// TODO: handle exception
			}
            return null;
        }

        try {
            if ("aes".equals(encryptType)) {
                // 是aes加密的消息
                String msgSignature = request.getParameter("msg_signature");
                WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), wxMpConfigStorage, timestamp, nonce, msgSignature, mpTag);
                WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
                if (outMessage == null) {
                    return null;
                }
//                response.getWriter().write(outMessage.toEncryptedXml(wxMpConfigStorage));
                return outMessage.toEncryptedXml(wxMpConfigStorage);
            }
        } catch (Exception e) {
            log.error("derypt msg failed.");
        }

//        response.getWriter().println("illegal encrypt type ...");
        // 不走 VM
        return JSON.toJSONString("illegal encrypt type ...");
    }

    @PostConstruct
    public void init() {
        
        wxMpMessageRouter = new WxMpMessageRouter(wxMpService);
        wxMpMessageRouter
        		
        		//若未关注公众号，扫描二维码，推送刮刮卡链接
		        .rule()
		        .msgType("event")
		        .event("subscribe")
		        .eventKey("qrscene_1")
		        .async(false)
		        .handler(new WxMpMessageHandler() {
		            @Override
		            public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
		            	sessionManager.getSession(wxMessage.getFromUserName());
		            	
		            	String mpTag = ConstantUtil.GUOGEGE_WX_CONF_KEY;
		            	String rUrl = "";
		            	String currentUrl = "";
		            	if (env.isProd()) {
		            		mpTag = ConstantUtil.GUOGEGE_WX_CONF_KEY;
		                	rUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/oauth.htm?redirect=";
		                	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/hActive.htm?page=scratchCard";
		                } else {
		                	mpTag = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
		                	rUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/oauth.htm?redirect=";
		                	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/hActive.htm?page=scratchCard";
		                }
		            	String wxUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(mpTag).getAppId()+"&redirect_uri=";
		                String wxUrlTail = "&response_type=code&scope=snsapi_base&state=" + mpTag + "&connect_redirect=2#wechat_redirect";
		                try {
		                	rUrl += URLEncoder.encode(currentUrl, "utf-8");
		                } catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
		                String toRedirectUrl = wxUrl + rUrl + wxUrlTail;
		            	
		            	Item wxArticle = new Item();
		            	wxArticle.setUrl(toRedirectUrl);
		        		wxArticle.setTitle("刮刮卡");
		        		//测试环境ticket
		        		//gQEH8DoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL0FraGpKM0htMmJhZzN5bW54V0R0AAIEwE8GVwMEAAAAAA==
		        		//生产环境ticket
		        		//gQGZ7zoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL19FU2k0SXJsRVNOb0xTSEV3bXlLAAIEUWEHVwMEAAAAAA==
		        		//wxArticle.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+wxMessage.getTicket());
		        		wxArticle.setPicUrl("http://mikumine.b0.upaiyun.com/active/images/scratchCard.jpg");
		        		
		        		return WxMpXmlOutMessage
		        			  .NEWS()
		        			  .fromUser(wxMessage.getToUserName())
		      				  .toUser(wxMessage.getFromUserName())
		      				  .addArticle(wxArticle)
		      				  .build();
		            }
		        })
		        .end()
		        
		        //若未关注公众号，扫描二维码，推送刮刮卡链接
		        .rule()
		        .msgType("event")
		        .event("SCAN")
		        .eventKey("1")
		        .async(false)
		        .handler(new WxMpMessageHandler() {
		            @Override
		            public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
		            	sessionManager.getSession(wxMessage.getFromUserName());
		            	String mpTag = ConstantUtil.GUOGEGE_WX_CONF_KEY;
		            	String rUrl = "";
		            	String currentUrl = "";
		            	if (env.isProd()) {
		            		mpTag = ConstantUtil.GUOGEGE_WX_CONF_KEY;
		                	rUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/oauth.htm?redirect=";
		                	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/hActive.htm?page=scratchCard";
		                } else {
		                	mpTag = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
		                	rUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/oauth.htm?redirect=";
		                	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/hActive.htm?page=scratchCard";
		                }
		            	String wxUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(mpTag).getAppId()+"&redirect_uri=";
		                String wxUrlTail = "&response_type=code&scope=snsapi_base&state=" + mpTag + "&connect_redirect=2#wechat_redirect";
		                try {
		                	rUrl += URLEncoder.encode(currentUrl, "utf-8");
		                } catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
		                String toRedirectUrl = wxUrl + rUrl + wxUrlTail;
		            	Item wxArticle = new Item();
		            	wxArticle.setUrl(toRedirectUrl);
		        		wxArticle.setTitle("刮刮卡兑奖");
		        		//wxArticle.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+wxMessage.getTicket());
		        		wxArticle.setPicUrl("http://mikumine.b0.upaiyun.com/active/images/scratchCard.jpg");
		        		
		        		return WxMpXmlOutMessage
		        			  .NEWS()
		        			  .fromUser(wxMessage.getToUserName())
		      				  .toUser(wxMessage.getFromUserName())
		      				  .addArticle(wxArticle)
		      				  .build();
		            }
		        })
		        .end()
        
                //
                .rule()
                .msgType("event")
                .event("CLICK")
                .eventKey("V1_SERVICE_PHONE")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        return WxMpXmlOutMessage.TEXT().content(
                                "全国统一服务热线：\n400-700-9939\n"
                        ).fromUser(wxMessage.getToUserName())
                                .toUser(wxMessage.getFromUserName()).build();
                    }
                })
                .end()
                
                .rule()
                .msgType("event")
                .event("subscribe")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        String content = "终于等到你，还好我没放弃，*_*，米酷为你准备了一麻袋的现金卷，让你任性买买买，马上领取："+BizConstants.ONLINE_DOMAIN+"/api/h/1.0/redPacket.htm \n\n";
                        content += "米酷是集社交·分享·零售·众筹·订制于一体的移动多元化互融平台，只要上米酷，你就是CEO，全场100%正品，7天无条件退换货";
                        WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(content).fromUser(wxMessage.getToUserName())
                                .toUser(wxMessage.getFromUserName()).build();
                        return m;
                    }
                })
                .end()
                
                .rule()
                .msgType("text")
                //.content("CONTENT")
                .rContent(".*")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        TransferCustomerServiceBuilder transferCustomerServiceBuilder = new TransferCustomerServiceBuilder();
                        return transferCustomerServiceBuilder.fromUser(wxMessage.getToUserName())
                        	.toUser(wxMessage.getFromUserName()).build();
                    }
                })
                .end();

                        //start 输入 2
                /*.rule()
                .msgType("text")
                .content("1")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        return WxMpXmlOutMessage.TEXT().content("支付宝，微信支付，我们全部都支持哟，嘻嘻 /:,@-D").fromUser(wxMessage.getToUserName())
                                .toUser(wxMessage.getFromUserName()).build();
                    }
                })
                .end()*/
                        //end 输入 2
                        //start 输入 3
                /*.rule()
                .msgType("text")
                .content("2")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        return WxMpXmlOutMessage.TEXT().content("客服热线：\n" +
                                        "全国统一服务热线：\n400-700-9939\n"
                        ).fromUser(wxMessage.getToUserName())
                                .toUser(wxMessage.getFromUserName()).build();
                    }
                })
                .end()*/
                        //end 输入 3
                /*.rule()
                .msgType("text")
                .rContent("^1\\d{10}$")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        if (employeeTradeQuery.isEmployee(wxMessage.getFromUserName())) {
                            Optional<String> stringOptional = employeeTradeQuery.query(StringUtils.trim(wxMessage.getContent()));
                            if (stringOptional.isPresent()) {
                                return WxMpXmlOutMessage.TEXT().content(stringOptional.get()).fromUser(wxMessage.getToUserName())
                                        .toUser(wxMessage.getFromUserName()).build();
                            }
                        }

                        return WxMpXmlOutMessage.TEXT().content("I, Robot, dial 4006831717 to contact us in emergency? ").fromUser(wxMessage.getToUserName())
                                .toUser(wxMessage.getFromUserName()).build();
                    }
                })
                .end()*/
                        // 兜底路由规则，一般放到最后

                
                //
                /*.rule()
                .msgType("text")
                .async(false)
                .handler(new WxMpMessageHandler() {
                    @Override
                    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
                        sessionManager.getSession(wxMessage.getFromUserName());
                        String content = "终于等到你，还好我没放弃，*_*，小酷为你准备了一麻袋的现金卷（马上领取："+BizConstants.ONLINE_DOMAIN+"/api/h/1.0/redPacket.htm），让你任性买买买 \n\n";
                        content += "米酷是专为国内女性打造的网络商城，全场100%正品，7天无条件退换货，你的美丽交由我打理！";
                        WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(content).fromUser(wxMessage.getToUserName())
                                .toUser(wxMessage.getFromUserName()).build();
                        
                        return m;
                    }
                })
                .end();*/

    }


    public InputStream StringTOInputStream(String in) throws Exception {

        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes("utf-8"));
        return is;
    }
    
    public static void main(String[] args) {
    	WxMpXmlOutTextMessage build = WxMpXmlOutMessage.TEXT().fromUser("客服")
        .toUser("顾客").build();
    	System.out.println(build.toXml());
    	System.out.println("-----------------------------------------");
    	
    	TransferCustomerServiceBuilder transferCustomerServiceBuilder = new TransferCustomerServiceBuilder();
        WxMpXmlOutTransferCustomerServiceMessage build2 = transferCustomerServiceBuilder.fromUser("客服")
        	.toUser("顾客").build();
        System.out.println(build2.toXml());
    	
	}
    
}
