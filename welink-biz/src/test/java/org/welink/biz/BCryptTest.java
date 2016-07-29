/**
 * Project Name:welink-web
 * File Name:BCryptTest.java
 * Package Name:com.welink.web.httpclient
 * Date:2015年9月22日下午4:42:17
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.welink.biz;

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
		String pswd = "65ab06eddf9dd82e664b67e1826e095b88250b2c450fb4dca32bf3f36bc9a1d46324db95a3d6b2fd807abab9545f7f24d135defae893e2eeb5818c90da9efdca389d5cb383a19a879911b5b73f837510822f877c00821646052b7ab887b9d5cfb2f82f49060c5959ffce69b874c5a46bcfe4fbbdf7691e447c457efb2c69b8cf";
		/*byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
        pswd = new String(pswdArray);*/
        /*String dePswd = PasswordParser.parserPlanPswd(pswd, null, false);
        String toStorePassword = BCrypt.hashpw(dePswd, BCrypt.gensalt());
        System.out.println("dePswd.."+dePswd);
        System.out.println("toStorePassword.."+toStorePassword);*/
        
        System.out.println("--------------------------------------------------");
        //System.out.println(BCrypt.checkpw(pswd, "$2a$10$1jBJjNiC3dOtbD8oV7X8OOES6qxpbNd0J5JBVnnJYQOcQ0GMBqH.G"));
        String dpswd = PasswordParser.parserPlanPswd(pswd, null, false);
        System.out.println("dpswd..........."+dpswd);
	}
}

