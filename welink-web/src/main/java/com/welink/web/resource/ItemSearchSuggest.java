/**
 * Project Name:welink-web
 * File Name:ItemSearchSuggest.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月11日下午4:09:43
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemSearchService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.domain.Item;

/**
 * ClassName:ItemSearchSuggest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月11日 下午4:09:43 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class ItemSearchSuggest {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(ItemSearchSuggest.class);
	
	@Resource
	ItemSearchService itemSearchService;
	
	@Resource
    private ItemService itemService;
	
	@Resource
    private UserService userService;

    @Resource
    private ShopService shopService;
	
	 @RequestMapping(value = {"/api/m/1.0/itemSearchSuggest.json", "/api/h/1.0/itemSearchSuggest.json"}, produces = "application/json;charset=utf-8")
	 public String itemSearchSuggest2(HttpServletRequest request, HttpServletResponse response,
			 @RequestParam(value="query",required=false, defaultValue="") String query) throws Exception {
		 query = java.net.URLDecoder.decode(query , "UTF-8");
		 Map<String, Object> cloudsearchSuggestMap = itemSearchService.cloudsearchSuggest(query);
         return JSON.toJSONString(cloudsearchSuggestMap);
	 }
	 
     @RequestMapping(value = {"/api/m/1.0/itemSearchSuggest2.json", "/api/h/1.0/itemSearchSuggest2.json"}, produces = "application/json;charset=utf-8")
	 public String itemSearchSuggest(HttpServletRequest request, HttpServletResponse response,
			 @RequestParam(value="query",required=false, defaultValue="") String query,
			 @RequestParam(value="pg", required = false, defaultValue="0")  Integer pg,  
				@RequestParam(value="sz", required = false, defaultValue="10")  Integer sz) throws Exception {
		 query = java.net.URLDecoder.decode(query , "UTF-8");
		 WelinkVO welinkVO = new WelinkVO();
		 Map<String, Object> resultMap = new HashMap<String, Object>();
		 if(null == query || "".equals(query.trim())){
			 resultMap.put("status", "0");
			 return JSON.toJSONString(resultMap);
		 }
         org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
         Session session = currentUser.getSession();
		 int page = pg;
		 int size = sz;
         if (size < 1) {
            size = 7;
         }
         if (page < 0) {
            page = 0;
         }
         int startRow = 0;
         startRow = (page) * size;
         //1.查找商品，走搜索
         long shopId = userService.fetchLastLoginShop(session);
         //long shopId = 2043L;
         shopId = shopService.fetchIdByShopId(shopId);
         log.info("根据shop_id获取id:" + shopId);
         
         try {
        	 List<Item> itemList = itemService.searchOpenSearchItems(shopId, query, null, null, startRow, size, OpenSearchType.WEIGHT_ASC.getType(), null, true);
             if (null != itemList && itemList.size() > 0) {
                 boolean hasNext = true;
                 if (null != itemList && itemList.size() < size) {
                     hasNext = false;
                 } else {
                     hasNext = true;
                 }
                 List<String> suggestions = new ArrayList<String>();
    			 for(Item item : itemList){
    				suggestions.add(item.getTitle());
    			 }
    			 resultMap.put("result", suggestions);
    			 resultMap.put("hasNext", hasNext);
    			 resultMap.put("status", "1");
             }else{
            	 resultMap.put("status", "0");
             }
		 } catch (Exception e) {
			 resultMap.put("status", "0");
		 }
         
         return JSON.toJSONString(resultMap);
	 }
	 
	 public static void main(String[] args) throws UnsupportedEncodingException {
		 String query = java.net.URLDecoder.decode("%E5%8F%91%E7%BB%99" , "UTF-8");
		 System.out.println("query....................."+query);
	}
}

