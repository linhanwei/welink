package com.welink.web.resource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swetake.util.Qrcode;
import com.welink.web.common.util.QRCodeUtils;
import com.welink.web.common.util.QRCodeZxingUtil;

/**
 * Created by daniel on 14-12-23.
 * 生成二维码
 */
@RestController
public class GenQr {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(GenQr.class);

    private String url = "https://open.weixin.qq.com/connect/oauth2/authorize";

    private String charset = "utf-8";

    private String AppId = "wx07d7e44f348d48f5";

    private String AppSecret = "a8a699b61085c4b0c250f453e3eb52f8";
    
    public static String defaultQRLogo = "http://mikumine.b0.upaiyun.com/1/0/cHJvZmlsZQ==/0/20151207/0-0-1449456659806-0.png";


    public String execute() throws Exception {
//        SSLHttpClientUtil httpClientUtil = new SSLHttpClientUtil();
//        //获取二维码
//        //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
//        String wholeAccTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + AppId + "&secret=" + AppSecret;
//        String wholeAccResult = httpClientUtil.doGet(wholeAccTokenUrl);
//        WholyAccTokenModel wAccToken = null;
//        if (StringUtils.isNotBlank(wholeAccResult)) {
//            wAccToken = JSON.parseObject(wholeAccResult, WholyAccTokenModel.class);
//        } else {
//            //TODO:获取token失败
//        }
//        String actionInfo = "{\"action_name\": \"QR_LIMIT_SCENE\",\"action_info\": {\"scene\": {\"scene_id\": 1000}}}";
//        String QRCodeUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + wAccToken.getAccess_token();
//        String QRCodeStr = httpClientUtil.doPostJson(QRCodeUrl, actionInfo, "utf-8");
//
//        WeChatTicketModel wTicket = null;
//        if (StringUtils.isNotBlank(QRCodeStr)){
//            wTicket = JSON.parseObject(QRCodeStr, WeChatTicketModel.class);
//        }else {
//            //TODO:error
//        }
//        //通过ticket换取二维码
//        //https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET
//        String qRcodeFetchUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+ URLEncoder.encode(wTicket.getTicket());
//        InputStream qrcode = httpClientUtil.doGetAsStream(qRcodeFetchUrl);
//
//        ServletActionContext.getContext().put("imageStream", qrcode);
        return "processImage";
    }
    
    @RequestMapping(value = {"/api/h/1.0/qrTest.json"}, produces = "application/json;charset=utf-8")
    public String qrTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String encodeddata = "http://miku.unesmall.com/api/h/1.0/indexPage.htm";
        qrCodeEncodeResponse(encodeddata, response);
    	return null;
    }
    
    @RequestMapping(value = {"/api/h/1.0/qrLogoTest.json"}, produces = "application/json;charset=utf-8")
    public String qrLogoTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String encodeddata = "http://miku.unesmall.com/api/h/1.0/indexPage.htm";
    	String ccbPath = "http://mikumine.b0.upaiyun.com/1/0/cHJvZmlsZQ==/0/20151207/0-0-1449456659806-0.png";
    	QRCodeUtils.createLoGoQRCodeHttpResponse(encodeddata, ccbPath, 8, response);
        //qrCodeEncodeResponse(encodeddata, response);
    	return null;
    }
    
    @RequestMapping(value = {"/api/m/1.0/qrUrl.json", "/api/h/1.0/qrUrl.json"}, produces = "application/json;charset=utf-8")
    public String qrUrl(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam String url) throws Exception {
        qrCodeEncodeResponse(url, response);
    	//QRCodeZxingUtil.encodeHttpResponse(url, null, response, true);
    	return null;
    }
    
    @RequestMapping(value = {"/api/h/1.0/qrLogoUrl.json"}, produces = "application/json;charset=utf-8")
    public String qrLogoUrl(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam String url, @RequestParam String logoUrl) throws Exception {
    	String encodeddata = "http://miku.unesmall.com/api/h/1.0/indexPage.htm";
    	QRCodeUtils.createLoGoQRCodeHttpResponse(encodeddata, logoUrl, 8, response);
    	return null;
    }
    
    /**
     * 编码字符串内容到目标httpResponse对象中
     * @param encodeddata
     * @param destFile
     * @throws IOException
     */
    public void qrCodeEncodeResponse(String encodeddata,HttpServletResponse response) throws IOException{
        Qrcode qrcode = new Qrcode();
        //错误修正容量   
        //L水平   7%的字码可被修正  
        //M水平   15%的字码可被修正  
        //Q水平   25%的字码可被修正  
        //H水平   30%的字码可被修正  
        //QR码有容错能力，QR码图形如果有破损，仍然可以被机器读取内容，最高可以到7%~30%面积破损仍可被读取。  
        //相对而言，容错率愈高，QR码图形面积愈大。所以一般折衷使用15%容错能力。
        qrcode.setQrcodeErrorCorrect('M');
        qrcode.setQrcodeEncodeMode('B');
        qrcode.setQrcodeVersion(7);
        byte[] d = encodeddata.getBytes("GBK");
        BufferedImage bi = new BufferedImage(139, 139, BufferedImage.TYPE_INT_RGB);
        // createGraphics
        Graphics2D g = bi.createGraphics();
        // set background
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, 139, 139);
        //设置二维码图片颜色
        g.setColor(Color.BLACK);
 
        //if (d.length > 0 && d.length < 123) {
        if (d.length > 0 && d.length < 500) {
            boolean[][] b = qrcode.calQrcode(d);
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < b.length; j++) {
                    if (b[j][i]) {
                        g.fillRect(j * 3 + 2, i * 3 + 2, 3, 3);
                    }
                }
            }
        }
 
        g.dispose();
        bi.flush();
 
        ImageIO.write(bi, "png", response.getOutputStream());
        System.out.println("Input Encoded data is："+encodeddata);
    }
    
}
