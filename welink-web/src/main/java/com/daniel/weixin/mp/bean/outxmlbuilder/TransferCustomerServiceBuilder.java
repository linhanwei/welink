/**
 * Project Name:welink-web
 * File Name:TransferCustomerServiceBuilder.java
 * Package Name:com.daniel.weixin.mp.bean.outxmlbuilder
 * Date:2016年3月25日下午12:44:14
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
 */

package com.daniel.weixin.mp.bean.outxmlbuilder;

import com.daniel.weixin.mp.bean.WxMpXmlOutTransferCustomerServiceMessage;

/**
 * ClassName:TransferCustomerServiceBuilder <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年3月25日 下午12:44:14 <br/>
 * 
 * @author LuoGuangChun
 * @version
 * @since JDK 1.6
 * @see
 */
public final class TransferCustomerServiceBuilder
		extends
		BaseBuilder<TransferCustomerServiceBuilder, WxMpXmlOutTransferCustomerServiceMessage> {
	private String kfAccount;

	public TransferCustomerServiceBuilder kfAccount(String kfAccount) {
		this.kfAccount = kfAccount;
		return this;
	}

	public WxMpXmlOutTransferCustomerServiceMessage build() {
		WxMpXmlOutTransferCustomerServiceMessage m = new WxMpXmlOutTransferCustomerServiceMessage();
		setCommon(m);
		m.setKfAccount(kfAccount);
		return m;
	}
	
}
