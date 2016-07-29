/**
 * Project Name:welink-web
 * File Name:PropertiesUtils.java
 * Package Name:com.welink.web.common.util
 * Date:2015年12月10日下午3:12:26
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.common.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * ClassName:PropertiesUtils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月10日 下午3:12:26 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class PropertiesUtils {
	private static PropertiesUtils instance;
	
	private Properties configProp;
	
	private PropertiesUtils(){}
	
	public static PropertiesUtils getInstance(){
		if(null == instance){
			instance = new PropertiesUtils();
		}
		return instance;
	}
	
	public void init() {
		String prifileName = null;
        String path = PropertiesUtils.class.getResource("/").getPath();
        File tmp = new File(path);
        String tmpPath = tmp.getParentFile().getParentFile().getPath();
        prifileName = tmpPath + "/WEB-INF/classes/" + "configPro.properties";
        //prifileName = "D:\\workspace\\guogegeWS\\welink\\welink-web\\src\\main\\resources\\configPro.properties";
        configProp = new Properties();
		try {
			//getClass().getResource("/mongodb.properties").getPath()
			InputStream conf = new BufferedInputStream(new FileInputStream(prifileName));
			configProp.load(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String key) {
		return configProp.getProperty(key);
	}
	
	public static void main(String[] args) throws InterruptedException {
		//while(true){
			PropertiesUtils propertiesUtils = PropertiesUtils.getInstance();
			propertiesUtils.init();
			Properties configProp2 = propertiesUtils.configProp;
			configProp2.list(System.out);
			/*Integer max_visit_count = Integer.valueOf(propertiesUtils.getProperty("max_visit_count"));
			System.out.println("max_visit_count: "+max_visit_count);
			Thread.sleep(1000L);*/
		//}
	}
	
}

