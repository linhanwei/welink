/**
 * Project Name:welink-web
 * File Name:BCryptTest.java
 * Package Name:com.welink.web.httpclient
 * Date:2015年9月22日下午4:42:17
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.httpclient;

import org.junit.Test;

import com.welink.biz.common.security.BCrypt;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;

/**
 * ClassName:BCryptTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年9月22日 下午4:42:17 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class BCryptTest {
	
	@Test
    public void password(){
		String pswd = "123456";
		/*byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
        pswd = new String(pswdArray);*/
        String dePswd = PasswordParser.parserPlanPswd(pswd, null, false);
        String toStorePassword = BCrypt.hashpw(dePswd, BCrypt.gensalt());
        System.out.println("dePswd.."+dePswd);
        System.out.println("toStorePassword.."+toStorePassword);
        
        System.out.println("--------------------------------------------------");
        System.out.println(BCrypt.checkpw(pswd, "$2a$10$AHkRnBLRYaRvaxfjWPI2R.Ogt574//.2JWV0X4Gp8Pwpx56/ZWvTC"));
	}
}

