package com.welink.biz.common.pay;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 14-12-5.
 */
public class XmlConverUtil {
    /**
     * map to xml xml <node><key label="key1">value1</key><key
     * label="key2">value2</key>......</node>
     *
     * @param map
     * @return
     */
    public static String maptoXml(Map map) {
        Document document = DocumentHelper.createDocument();

        Element nodeElement = document.addElement("direct_trade_create _req");
        for (Object obj : map.keySet()) {
            Element keyElement = nodeElement.addElement(obj.toString());
            keyElement.setText(String.valueOf(map.get(obj)));
        }
        String result = document.asXML();
        result = StringUtils.remove(result, "\n");
        result = StringUtils.remove(result, "\r");
        result = StringUtils.remove(result, " ");
        result = StringUtils.remove(result, "<?xmlversion=\"1.0\"encoding=\"UTF-8\"?>");
        result = StringUtils.remove(result, "&");
        return result;
    }

    public static void main(String args[]) {
        XmlConverUtil util = new XmlConverUtil();
        Map<String, String> map = new HashMap<>();
        map.put("price", "102");
        map.put("title", "haha");
        map.put("buyerId", "980");
        String s = XmlConverUtil.maptoXml(map);
        System.out.println(s);
    }
}
