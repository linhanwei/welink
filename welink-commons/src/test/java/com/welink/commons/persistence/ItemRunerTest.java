/**
 * Project Name:welink-commons
 * File Name:ItemRunerTest.java
 * Package Name:com.welink.commons.persistence
 * Date:2015年11月19日下午9:42:35
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;
/**
 * ClassName:ItemRunerTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月19日 下午9:42:35 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ItemRunerTest {

	public static void main(String[] args) {
		ItemRuner item = new ItemRuner(null, null, "lgc");
		Thread tr = new Thread(item);
		tr.start();
		System.out.println();
		Thread tr2 = new Thread(item);
		tr2.start();
	}
	

}

