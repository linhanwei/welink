/**
 * Project Name:welink-web
 * File Name:WxMpXmlOutTransferCustomerServiceMessage.java
 * Package Name:com.daniel.weixin.mp.bean
 * Date:2016年3月25日下午12:40:32
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.daniel.weixin.mp.bean;

import com.daniel.weixin.common.util.WxConsts;
import com.daniel.weixin.common.util.xml.XStreamCDataConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * ClassName:WxMpXmlOutTransferCustomerServiceMessage <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月25日 下午12:40:32 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@XStreamAlias("xml")
public class WxMpXmlOutTransferCustomerServiceMessage extends WxMpXmlOutMessage {
  @XStreamAlias("TransInfo")
  protected final TransInfo transInfo = new TransInfo();

  public WxMpXmlOutTransferCustomerServiceMessage() {
    this.msgType = WxConsts.XML_TRANSFER_CUSTOMER_SERVICE;
  }

  public String getKfAccount() {
    return transInfo.getKfAccount();
  }

  public void setKfAccount(String kfAccount) {
    transInfo.setKfAccount(kfAccount);
  }

  @XStreamAlias("TransInfo")
  public static class TransInfo {

    @XStreamAlias("KfAccount")
    @XStreamConverter(value=XStreamCDataConverter.class)
    private String kfAccount;

    public String getKfAccount() {
      return kfAccount;
    }

    public void setKfAccount(String kfAccount) {
      this.kfAccount = kfAccount;
    }
  }
  
  @Override
  public String toXml() {
	  return config_WxMpXmlOutTransferCustomerServiceMessage().toXML(this);
  }
  
  private static com.thoughtworks.xstream.XStream config_WxMpXmlOutTransferCustomerServiceMessage() {
    com.thoughtworks.xstream.XStream xstream = com.daniel.weixin.common.util.xml.XStreamInitializer.getInstance();
    xstream.processAnnotations(com.daniel.weixin.mp.bean.WxMpXmlOutMessage.class);
    xstream.processAnnotations(com.daniel.weixin.mp.bean.WxMpXmlOutTransferCustomerServiceMessage.class);
    return xstream;
  }
  
}
