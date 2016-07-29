package com.welink.biz.common.security;

import com.welink.biz.common.constants.ResourcesConstants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 解密
 *
 * @author yonder
 */
public class PasswordParser {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PasswordParser.class);

    public static String parserPlanPswd(String oriPswd, String privateKey, boolean h5) {
        //load private key in case
        loadPriKeyInCase();
    	//loadPublicKeyInCase();	//自己加的
        //encrypt password
        String dePswd = doEncrypt(oriPswd, h5);
        return dePswd;
    }

    /**
     * encrypt password
     *
     * @param oriPswd
     * @return
     */
    private static String doEncrypt(String oriPswd, boolean h5) {
        String dePswd = "";
        byte[] decodedData = null;
        try {
            decodedData = RSAEncrypt.decrypt(RSAEncrypt.getPrivateKey(), AESUtil.parseHexStr2Byte(oriPswd));//RSAUtils.decryptByPrivateKey(AESUtil.parseHexStr2Byte(oriPswd), privateKey);
        } catch (Exception e) {
            log.error("encrypt failed . exp :" + e.getMessage() + ",oriPswd:" + oriPswd);
        }
        if (null != decodedData) {
            try {
                dePswd = new String(decodedData, "UTF-8");
            	//dePswd = new sun.misc.BASE64Encoder().encodeBuffer(decodedData);
                String newdePswd = dePswd.replaceAll("[\u0000-\u001f]", "");
                if (h5) {
                    String hdePswd = StringUtils.reverse(newdePswd);
                    byte[] ends1 = AESUtil.parseHexStr2Byte(hdePswd);
                    dePswd = new String(ends1, "utf-8");
                    //dePswd = new sun.misc.BASE64Encoder().encodeBuffer(ends1);
                    return dePswd;
                }
                byte[] ends1 = AESUtil.parseHexStr2Byte(newdePswd);
                dePswd = new String(ends1, "UTF-8");
                dePswd = dePswd.replaceAll("[\u0000-\u001f]", "");
            } catch (UnsupportedEncodingException e) {
                log.error("decrypt password ... encode error exp:" + e.getLocalizedMessage() + "______" + e.getMessage() + "oriPswd:" + oriPswd);
            } catch (Exception e) {
                log.error("decrypt password ... encode error exp:" + e.getLocalizedMessage() + "______" + e.getMessage() + "oriPswd:" + oriPswd);
            }
        }
        return dePswd;
    }

    /*
     *load private key from file in case
     */
    private static void loadPriKeyInCase() {

        String prifileName = ResourcesConstants.PRIVATE_KEY_PATH;
        //String prifileName = ServletActionContext.getServletContext().getRealPath("/")+"WEB-INF/classes/"+ResourcesConstants.PRIVATE_KEY_PATH;
        String path = PasswordParser.class.getResource("/").getPath();
        File tmp = new File(path);
        String tmpPath = tmp.getParentFile().getParentFile().getPath();
        prifileName = tmpPath + "/WEB-INF/classes/" + ResourcesConstants.PRIVATE_KEY_PATH;
        //prifileName = "D:\\workspace\\guogegeWS\\welink\\welink-web\\src\\main\\resources\\private.key";
        File file = new File(prifileName);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            log.error("private key file not find. exp:" + e1.getMessage());
        }
        if (!RSAEncrypt.isPrivateKeyLoaded()) {
            RSAEncrypt rsaEncrypt = new RSAEncrypt();
            if (null != in) {
                try {
                    rsaEncrypt.loadPrivateKey(in);
                } catch (Exception e) {
                    log.error("load private key failed . exp:" + e.getMessage());
                }
            }
        }
    }

    /*
    *load public key from file in case
    */
    public static void loadPublicKeyInCase() {

        String prifileName = null;
        //String prifileName = ServletActionContext.getServletContext().getRealPath("/")+"WEB-INF/classes/"+ResourcesConstants.PRIVATE_KEY_PATH;
        String path = PasswordParser.class.getResource("/").getPath();
        File tmp = new File(path);
        String tmpPath = tmp.getParentFile().getParentFile().getPath();
        prifileName = tmpPath + "/WEB-INF/classes/" + ResourcesConstants.PUBLIC_KEY_PATH;
        //prifileName = "D:\\workspace\\guogegeWS\\welink\\welink-web\\src\\main\\resources\\" + ResourcesConstants.PUBLIC_KEY_PATH;;
        File file = new File(prifileName);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            log.error("public key file not find. exp:" + e1.getMessage());
        }
        if (!RSAEncrypt.isPublicKeyLoaded()) {
            RSAEncrypt rsaEncrypt = new RSAEncrypt();
            if (null != in) {
                try {
                    rsaEncrypt.loadPublicKey(in);
                } catch (Exception e) {
                    log.error("load public key failed . exp:" + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        String path = PasswordParser.class.getResource("/").getPath();
        File tmp = new File(path);
        String tmpPath = tmp.getParentFile().getParentFile().getParentFile().getPath();
        tmpPath = tmpPath + "/WEB-INF/classes/" + ResourcesConstants.PRIVATE_KEY_PATH;
        tmpPath = "D:\\workspace\\guogegeWS\\welink\\welink-web\\src\\main\\resources\\private.key";
        System.out.println(tmpPath);
        String pswd = "8fa258ee84a611c56cc99ee631ffd46dc13a4112dbf980b23e44ce222315e5f9ccc6ced3a2a83f2f9b407cd7f780ac4c96061d8a30e23b3f4f4548458888665fa55df0c5d2b7734995cd28aa24b936ba262854bafedb7c5475e05e828953181398b39e623e6a995665767c14f26a960bd321dc79866d4e56d0e89c988bdb3e63";
        pswd = "65ab06eddf9dd82e664b67e1826e095b88250b2c450fb4dca32bf3f36bc9a1d46324db95a3d6b2fd807abab9545f7f24d135defae893e2eeb5818c90da9efdca389d5cb383a19a879911b5b73f837510822f877c00821646052b7ab887b9d5cfb2f82f49060c5959ffce69b874c5a46bcfe4fbbdf7691e447c457efb2c69b8cf";
        System.out.println(PasswordParser.parserPlanPswd(pswd, null, false));
        
    }

    private static int checkMsgLen(String len) {
        int l = 0;
        if (StringUtils.isNotBlank(len)) {
            l = Integer.valueOf(len);
        }
        return l;
    }
}
