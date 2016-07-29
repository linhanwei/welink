/**
 * Project Name:welink-web
 * File Name:ItemchangeControler.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月16日下午11:51:11
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.welink.commons.domain.Item;
import com.welink.commons.persistence.ItemMapper;

/**
 * ClassName:ItemchangeControler <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月16日 下午11:51:11 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class ItemchangeControler {
	
	@Resource
	private ItemMapper ItemMapper;
	
	 @RequestMapping(value = {"/api/m/1.0/itemChange.json", "/api/h/1.0/itemChange.json"}, produces = "application/json;charset=utf-8")
	 public String execute(HttpServletRequest request, HttpServletResponse response,
			 @RequestParam Long id, @RequestParam Integer num) throws Exception {
		 Item item = new Item();
		 item.setId(id);
		 item.setNum(num);
		 //ItemMapper.updateByPrimaryKey(item);
		 ItemMapper.updateByPrimaryKeySelective(item);
		 return null;
	 }
}

