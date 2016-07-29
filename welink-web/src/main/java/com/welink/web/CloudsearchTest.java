/**
 * Project Name:welink-web
 * File Name:CloudsearchTest.java
 * Package Name:com.welink.web
 * Date:2015年11月11日下午5:44:53
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.aliyun.opensearch.CloudsearchClient;
import com.aliyun.opensearch.CloudsearchSuggest;
import com.aliyun.opensearch.object.KeyTypeEnum;
import com.welink.commons.commons.BizConstants;


/**
 * ClassName:CloudsearchTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月11日 下午5:44:53 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class CloudsearchTest {

	public static void main(String[] args) {
        try {
            Map<String, Object> opts = new HashMap<String, Object>();
            /*String accessKeyId = "GDheOkyZuLg7VALU";
            String accessKeySecret = "7sQA8nMHZkB3CNgspOWnpzrl5B7tx0";*/
            String host = "http://opensearch-cn-hangzhou.aliyuncs.com";
            
            String accessKeyId = BizConstants.ALIYUN_ACCESSKEY_ID_PRO;
            String accessKeySecret = BizConstants.ALIYUN_ACCESSKEY_SECRET_PRO;
            CloudsearchClient client = new CloudsearchClient(accessKeyId, accessKeySecret, host, opts, KeyTypeEnum.ALIYUN);
            
            String indexName = "miku_item_search";
            String suggestName = "title";
            CloudsearchSuggest suggest = new CloudsearchSuggest(indexName, suggestName, client);

            suggest.setHit(10);
            suggest.setQuery("滋");
            String result = suggest.search();

            JSONObject jsonResult = new JSONObject(result);
            List<String> suggestions = new ArrayList<String>();

            if (!jsonResult.has("errors")) {
                JSONArray itemsJsonArray = (JSONArray) jsonResult.get("suggestions");
                for (int i = 0; i < itemsJsonArray.length(); i++){
                    JSONObject item = (JSONObject) itemsJsonArray.get(i);
                    suggestions.add(item.getString("suggestion"));
                }
                Map<String,Object> ret = new HashMap<String,Object>();
                ret.put("result",suggestions);
                ret.put("status","OK");
                System.out.println(new JSONObject(ret).toString());
            } else {
            	System.out.println("1111111111111111111");
            	System.out.println("2222222222222222222222222222222222");
            }
        } catch (UnknownHostException e) {
            ;
        } catch (ClientProtocolException e) {
            ;
        } catch (IOException e) {
            ;
        }
    }
	
}

