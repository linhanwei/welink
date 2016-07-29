package com.welink.web.common.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.tencent.common.Configure;
import com.tencent.common.RandomStringGenerator;
import com.tencent.common.Signature;

public class ReportTransferData {
	private String mch_appid;
    private String mchid ;
    private String device_info ;
    private String nonce_str ;
    private String sign ;
    private String partner_trade_no ;
    private String openid ;
    private String re_user_name ;
    private int amount;
    private String desc ;
    private String spbill_create_ip ;
    private String check_name = "";
    


    
	public String getCheck_name() {
		return check_name;
	}

	public void setCheck_name(String check_name) {
		this.check_name = check_name;
	}

	public ReportTransferData(String tradeno,String openid,int price) {
		setMch_appid("wx82d4b04a531ac1a3");
		setMchid("1242526802");
		setDevice_info("apple");
		setNonce_str(RandomStringGenerator.getRandomStringByLength(32));
		setPartner_trade_no(tradeno);
		//我们的系统有app的openid与微信公众好的openid【profile_wechat】
		setOpenid(openid);
//		setRe_user_name("赖豪达");
		setAmount(price);
		//NO_CHECK 不校验     OPTION_CHECK:校验
		setCheck_name("NO_CHECK");
		setDesc("MIKU MINE");
//		setDesc("红包");
		setSpbill_create_ip("121.26.217.212");
		//根据API给的签名规则进行签名
        String sign = Signature.getSign(toMap());
        setSign(sign);//把签名数据设置到Sign这个属性中
	}
	
	public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if(obj!=null){
                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }



	public String getMch_appid() {
		return mch_appid;
	}




	public void setMch_appid(String mch_appid) {
		this.mch_appid = mch_appid;
	}




	public String getMchid() {
		return mchid;
	}




	public void setMchid(String mchid) {
		this.mchid = mchid;
	}




	public String getDevice_info() {
		return device_info;
	}




	public void setDevice_info(String device_info) {
		this.device_info = device_info;
	}




	public String getNonce_str() {
		return nonce_str;
	}




	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}




	public String getSign() {
		return sign;
	}




	public void setSign(String sign) {
		this.sign = sign;
	}




	public String getPartner_trade_no() {
		return partner_trade_no;
	}




	public void setPartner_trade_no(String partner_trade_no) {
		this.partner_trade_no = partner_trade_no;
	}




	public String getOpenid() {
		return openid;
	}




	public void setOpenid(String openid) {
		this.openid = openid;
	}




	public String getRe_user_name() {
		return re_user_name;
	}




	public void setRe_user_name(String re_user_name) {
		this.re_user_name = re_user_name;
	}




	public int getAmount() {
		return amount;
	}




	public void setAmount(int amount) {
		this.amount = amount;
	}




	public String getDesc() {
		return desc;
	}




	public void setDesc(String desc) {
		this.desc = desc;
	}




	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}




	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}
}
