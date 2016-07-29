package com.welink.biz.common.pay;

/**
 * Created by daniel on 14-12-5.
 */

import com.welink.biz.common.security.Base64Utils;
import org.apache.commons.httpclient.NameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.util.*;

/* *
 *类名：AlipaySubmit
 *功能：支付宝各接口请求提交类
 *详细：构造支付宝各接口表单HTML文本，获取远程HTTP数据
 *版本：3.2
 *日期：2011-03-17
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipaySubmit {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AlipaySubmit.class);

    /**
     * 支付宝消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";


    /**
     * 验证消息是否是支付宝发出的合法消息，验证callback
     *
     * @param params 通知返回来的参数数组
     * @return 验证结果
     */
    public static boolean verifyReturn(Map<String, String> params) {
        String sign = "";
        //获取返回时的签名验证结果
        if (params.get("sign") != null) {
            sign = params.get("sign");
        }
        //验证签名
        boolean isSign = getSignVeryfy(params, sign, true);

        //写日志记录（若要调试，请取消下面两行注释）
        //String sWord = "isSign=" + isSign + "\n 返回回来的参数：" + AlipayCore.createLinkString(params);
        //AlipayCore.logResult(sWord);

        //判断isSign是否为true
        //isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
        if (isSign) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据反馈回来的信息，生成签名结果
     *
     * @param Params 通知返回来的参数数组
     * @param sign   比对的签名结果
     * @param isSort 是否排序
     * @return 生成的签名结果
     */
    private static boolean getSignVeryfy(Map<String, String> Params, String sign, boolean isSort) {
        //过滤空值、sign与sign_type参数
        Map<String, String> sParaNew = AlipayCore.paraFilter(Params);
        //获取待签名字符串
        String preSignStr = "";
        if (isSort) {
            preSignStr = AlipayCore.createLinkString(sParaNew);
        } else {
            preSignStr = AlipayCore.createLinkStringNoSort(sParaNew);
        }
        //获得签名验证结果
        boolean isSign = false;
        if (AlipayConfig.sign_type.equals("MD5")) {
            isSign = MD5.verify(preSignStr, sign, AlipayConfig.key, AlipayConfig.input_charset);
        }
        if (AlipayConfig.sign_type.equals("0001")) {
            isSign = RSA.verify(preSignStr, sign, AlipayConfig.ali_public_key, AlipayConfig.input_charset);
        }
        return isSign;
    }

    /**
     * 验证消息是否是支付宝发出的合法消息，验证服务器异步通知
     *
     * @param params 通知返回来的参数数组
     * @return 验证结果
     */
    public static boolean verifyNotify(Map<String, String> params) throws Exception {

        //获取是否是支付宝服务器发来的请求的验证结果
        String responseTxt = "true";
        try {
            //XML解析notify_data数据，获取notify_id
            Document document = DocumentHelper.parseText(params.get("notify_data"));
            String notify_id = document.selectSingleNode("//notify/notify_id").getText();
            responseTxt = verifyResponse(notify_id);
        } catch (Exception e) {
            responseTxt = e.toString();
        }

        //获取返回时的签名验证结果
        String sign = "";
        if (params.get("sign") != null) {
            sign = params.get("sign");
        }
        boolean isSign = getSignVeryfy(params, sign, false);

        //写日志记录（若要调试，请取消下面两行注释）
        //String sWord = "responseTxt=" + responseTxt + "\n isSign=" + isSign + "\n 返回回来的参数：" + AlipayCore.createLinkString(params);
        //AlipayCore.logResult(sWord);

        //判断responsetTxt是否为true，isSign是否为true
        //responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
        //isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
        if (isSign && responseTxt.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取远程服务器ATN结果,验证返回URL
     *
     * @param notify_id 通知校验ID
     * @return 服务器ATN结果
     * 验证结果集：
     * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空
     * true 返回正确信息
     * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
     */
    private static String verifyResponse(String notify_id) {
        //获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求

        String partner = AlipayConfig.partner;
        String veryfy_url = HTTPS_VERIFY_URL + "partner=" + partner + "&notify_id=" + notify_id;

        return checkUrl(veryfy_url);
    }

    /**
     * 获取远程服务器ATN结果
     *
     * @param urlvalue 指定URL路径地址
     * @return 服务器ATN结果
     * 验证结果集：
     * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空
     * true 返回正确信息
     * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
     */
    private static String checkUrl(String urlvalue) {
        String inputLine = "";

        try {
            URL url = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                    .getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception e) {
            e.printStackTrace();
            inputLine = "";
        }

        return inputLine;
    }

    /**
     * 生成要请求给支付宝的参数数组
     *
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static LinkedHashMap<String, String> buildRequestParaForH5(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        LinkedHashMap<String, String> sPara = AlipayCore.paraFilterForH5(sParaTemp);
        //排序
        String values = AlipayCore.createLinkString(sPara);
        //生成签名结果
        String mysign = RSA.sign(values, AlipayConfig.input_charset);
        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
//        log.error("=========mysign=========:" + mysign);
        return sPara;
    }

    /**
     * 生成要请求给支付宝的参数数组
     *
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //排序
        String values = AlipayCore.createLinkString(sPara);
        /*
        String content = SignatureUtil.buildSignData(sPara);
        String sign = null;
        try {
        	SignatureExecutor signatureExecutor = new SignatureExecutor();
            sign = signatureExecutor.sign(SignType.MD5, AlipayConfig.key, content, AlipayConfig.input_charset);
            System.out.println("sign="+sign);
        } catch (SignatureException e) {
            e.printStackTrace();
        }*/

        //生成签名结果
        String mysign = RSA.sign(values, AlipayConfig.input_charset);
        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AlipayConfig.sign_type);
        return sPara;
    }

    /**
     * 构造提交表单HTML数据
     *
     * @param sParaTemp     请求参数数组
     * @param gateway       网关地址
     * @param strMethod     提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     */
    public static String buildForm(Map<String, String> sParaTemp, String gateway, String strMethod,
                                   String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>" +
                "<html xmlns='http://www.w3.org/1999/xhtml'><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8' /></head>");
        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + gateway
                + "_input_charset=" + AlipayConfig.input_charset + "\" method=\"" + strMethod
                + "\">");
        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

        return sbHtml.toString();
    }

    /**
     * MAP类型数组转换成NameValuePair类型
     *
     * @param properties MAP类型数组
     * @return NameValuePair类型数组
     */
    private static NameValuePair[] generatNameValuePair(Map<String, String> properties) {
        NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
        }

        return nameValuePair;
    }


    /**
     * 构造模拟远程HTTP的POST请求，获取支付宝的返回XML处理结果
     *
     * @param sParaTemp 请求参数数组
     * @param gateway   网关地址
     * @return 支付宝返回XML处理结果
     * @throws Exception
     */
    public static String sendPostInfo(Map<String, String> sParaTemp, String gateway)
            throws Exception {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);

        HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();

        HttpRequest request = new HttpRequest(HttpResultType.BYTES);
        //设置编码集
        request.setCharset(AlipayConfig.input_charset);

        request.setParameters(generatNameValuePair(sPara));
        request.setUrl(gateway + "_input_charset=" + AlipayConfig.input_charset);

        HttpResponse response = httpProtocolHandler.execute(request);
        if (response == null) {
            return null;
        }

        String strResult = response.getStringResult();

        return strResult;
    }


    /**
     * 生成签名结果
     *
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestMysign(Map<String, String> sPara) {
        String prestr = AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        if (AlipayConfig.sign_type.equals("MD5")) {
            mysign = MD5.sign(prestr, AlipayConfig.key, AlipayConfig.input_charset);
        }
        if (AlipayConfig.wap_sign_type.equals("0001")) {
            mysign = RSA.sign(prestr, AlipayConfig.private_key, AlipayConfig.input_charset);
        }
        return mysign;
    }

    /**
     * 生成要请求给支付宝的参数数组
     *
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildRequestParaNew(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildRequestMysign(sPara);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        if (!sPara.get("service").equals("alipay.wap.trade.create.direct") && !sPara.get("service").equals("alipay.wap.auth.authAndExecute")) {
            sPara.put("sign_type", AlipayConfig.sign_type);
        }

        return sPara;
    }

    /**
     * 建立请求，以表单HTML形式构造（默认）
     *
     * @param sParaTemp     请求参数数组
     * @param strMethod     提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
     */
    public static String buildRequestNew(String ALIPAY_GATEWAY_NEW, Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestParaNew(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + ALIPAY_GATEWAY_NEW
                + "_input_charset=" + AlipayConfig.input_charset + "\" method=\"" + strMethod
                + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

        return sbHtml.toString();
    }

    /**
     * 建立请求，以表单HTML形式构造，带文件上传功能
     *
     * @param sParaTemp       请求参数数组
     * @param strMethod       提交方式。两个值可选：post、get
     * @param strButtonName   确认按钮显示文字
     * @param strParaFileName 文件上传的参数名
     * @return 提交表单HTML文本
     * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
     */
    public static String buildRequest(String ALIPAY_GATEWAY_NEW, Map<String, String> sParaTemp, String strMethod, String strButtonName, String strParaFileName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\"  enctype=\"multipart/form-data\" action=\"" + ALIPAY_GATEWAY_NEW
                + "_input_charset=" + AlipayConfig.input_charset + "\" method=\"" + strMethod
                + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        sbHtml.append("<input type=\"file\" name=\"" + strParaFileName + "\" />");

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");

        return sbHtml.toString();
    }

    /**
     * 建立请求，以模拟远程HTTP的POST请求方式构造并获取支付宝的处理结果
     * 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值
     * 如：buildRequest("", "",sParaTemp)
     *
     * @param strParaFileName 文件类型的参数名
     * @param strFilePath     文件路径
     * @param sParaTemp       请求参数数组
     * @return 支付宝处理结果
     * @throws Exception
     * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
     */
    public static String buildRequest(String ALIPAY_GATEWAY_NEW, String strParaFileName, String strFilePath, Map<String, String> sParaTemp) throws Exception {
        //待请求参数数组
        Map<String, String> sPara = buildRequestParaForH5(sParaTemp);

        HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();

        HttpRequest request = new HttpRequest(HttpResultType.BYTES);
        //设置编码集
        request.setCharset(AlipayConfig.input_charset);

        request.setParameters(generatNameValuePair(sPara));
        request.setUrl(ALIPAY_GATEWAY_NEW/* + "_input_charset=" + AlipayConfig.input_charset*/);

        HttpResponse response = httpProtocolHandler.execute(request, strParaFileName, strFilePath);
        if (response == null) {
            return null;
        }

        String strResult = response.getStringResult();

        return strResult;
    }

    /**
     * 建立请求，以表单HTML形式构造（默认）
     *
     * @param sParaTemp     请求参数数组
     * @param strMethod     提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
     */
    public static String buildRequestForPay(String ALIPAY_GATEWAY_NEW, Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        LinkedHashMap<String, String> sPara = buildRequestParaForH5(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + ALIPAY_GATEWAY_NEW
               /* + "_input_charset=" + AlipayConfig.input_charset */ + "\" method=\"" + strMethod
                + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

        return sbHtml.toString();
    }


    /**
     * 构建URL用于支付宝支付在微信中可用
     * 如果微信解封了alipay，请使用上面的方法  buildRequestForPay
     *
     * @param ALIPAY_GATEWAY_NEW
     * @param sParaTemp
     * @param strMethod
     * @param strButtonName
     * @return
     */
    public static String buildRequestForPayUrl(String ALIPAY_GATEWAY_NEW, Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        LinkedHashMap<String, String> sPara = buildRequestParaForH5(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer url = new StringBuffer();
        //url.append(ALIPAY_GATEWAY_NEW);

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);
            url.append(name + "=" + URLEncoder.encode(value) + "&");
        }
        return url.toString();
    }

    /**
     * 解析远程模拟提交后返回的信息，获得token
     *
     * @param text 要解析的字符串
     * @return 解析结果
     * @throws Exception
     */
    public static String getRequestToken(String text) throws Exception {
        String request_token = "";
        //以“&”字符切割字符串
        String[] strSplitText = text.split("&");
        //把切割后的字符串数组变成变量与数值组合的字典数组
        Map<String, String> paraText = new HashMap<String, String>();
        for (int i = 0; i < strSplitText.length; i++) {

            //获得第一个=字符的位置
            int nPos = strSplitText[i].indexOf("=");
            //获得字符串长度
            int nLen = strSplitText[i].length();
            //获得变量名
            String strKey = strSplitText[i].substring(0, nPos);
            //获得数值
            String strValue = strSplitText[i].substring(nPos + 1, nLen);
            //放入MAP类中
            paraText.put(strKey, strValue);
        }

        if (paraText.get("res_data") != null) {
            String res_data = paraText.get("res_data");
            //解析加密部分字符串（RSA与MD5区别仅此一句）
            if (AlipayConfig.sign_type.equals("RSA")) {
                Key key = (Key) AlipayConfig.PARTNER_PRIVATE_KEY;
                res_data = RSA.decrypt(res_data, Base64Utils.encode(key.getEncoded()), AlipayConfig.input_charset);
            }

            //token从res_data中解析出来（也就是说res_data中已经包含token的内容）
            Document document = DocumentHelper.parseText(res_data);
            request_token = document.selectSingleNode("//direct_trade_create_res/request_token").getText();
        }
        return request_token;
    }

    /**
     * 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数
     * 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
     *
     * @return 时间戳字符串
     * @throws IOException
     * @throws DocumentException
     * @throws MalformedURLException
     */
    public static String query_timestamp() throws MalformedURLException,
            DocumentException, IOException {

        //构造访问query_timestamp接口的URL串
        String strUrl = "https://mapi.alipay.com/gateway.do?service=query_timestamp&partner=" + AlipayConfig.partner;
        StringBuffer result = new StringBuffer();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new URL(strUrl).openStream());

        List<Node> nodeList = doc.selectNodes("//alipay/*");

        for (Node node : nodeList) {
            // 截取部分不需要解析的信息
            if (node.getName().equals("is_success") && node.getText().equals("T")) {
                // 判断是否有成功标示
                List<Node> nodeList1 = doc.selectNodes("//response/timestamp/*");
                for (Node node1 : nodeList1) {
                    result.append(node1.getText());
                }
            }
        }

        return result.toString();
    }
}