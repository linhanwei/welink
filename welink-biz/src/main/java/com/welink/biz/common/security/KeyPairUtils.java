package com.welink.biz.common.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

public class KeyPairUtils implements InitializingBean {

    static HashMap<String, String> pubkey = new HashMap<String, String>();
    static HashMap<String, String> prikey = new HashMap<String, String>();
    static HashMap<String, String> pubkeyNoBase = new HashMap<String, String>();

    //测试用，用来初始化生成密钥对
    @SuppressWarnings("rawtypes")
    public void afterPropertiesSet() throws Exception {
        Map keyMap = RSAUtils.genKeyPair();
        String publicKey = RSAUtils.getPublicKey(keyMap);
        String privateKey = RSAUtils.getPrivateKey(keyMap);
        String privateKeyNoBase = RSAUtils.getPublicKeyNoBase64(keyMap);//(keyMap);
        pubkey.put("1", publicKey);
        prikey.put("1", privateKey);
        pubkeyNoBase.put("1", privateKeyNoBase);
    }

    public static String fetchPubKey(String uid) {
        if (StringUtils.isNotBlank(uid)) {
            return pubkey.get(uid);
        } else {
            return null;
        }
    }

    public static String fetchPubKeyNoBase64(String uid) {
        if (StringUtils.isNotBlank(uid)) {
            return pubkeyNoBase.get(uid);
        } else {
            return null;
        }
    }

    public static String fetchPrivateKey(String uid) {
        if (StringUtils.isNotBlank(uid)) {
            return prikey.get(uid);
        } else {
            return null;
        }
    }
}
