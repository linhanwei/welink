package com.welink.web.common.util;

 
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;
import jp.sourceforge.qrcode.exception.DecodingFailedException;

import com.swetake.util.Qrcode;
import com.welink.biz.common.security.BCrypt;
 
public class QRCodeUtils {
	public static String defaultQRLogo = "http://mikumine.b0.upaiyun.com/1/0/cHJvZmlsZQ==/0/20151207/0-0-1449456659806-0.png";
 
    /**
     * 编码字符串内容到目标File对象中
     * @param encodeddata
     * @param destFile
     * @throws IOException
     */
    public static void qrCodeEncode(String encodeddata,File destFile) throws IOException{
        Qrcode qrcode = new Qrcode();
        //错误修正容量   
        //L水平   7%的字码可被修正  
        //M水平   15%的字码可被修正  
        //Q水平   25%的字码可被修正  
        //H水平   30%的字码可被修正  
        //QR码有容错能力，QR码图形如果有破损，仍然可以被机器读取内容，最高可以到7%~30%面积破损仍可被读取。  
        //相对而言，容错率愈高，QR码图形面积愈大。所以一般折衷使用15%容错能力。
        qrcode.setQrcodeErrorCorrect('M');
        qrcode.setQrcodeEncodeMode('B');
        qrcode.setQrcodeVersion(7);
        byte[] d = encodeddata.getBytes("GBK");
        BufferedImage bi = new BufferedImage(139, 139, BufferedImage.TYPE_INT_RGB);
        // createGraphics
        Graphics2D g = bi.createGraphics();
        // set background
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, 139, 139);
        //设置二维码图片颜色
        g.setColor(Color.BLACK);
 
        if (d.length > 0 && d.length < 123) {
            boolean[][] b = qrcode.calQrcode(d);
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < b.length; j++) {
                    if (b[j][i]) {
                        g.fillRect(j * 3 + 2, i * 3 + 2, 3, 3);
                    }
                }
            }
        }
 
        g.dispose();
        bi.flush();
 
        ImageIO.write(bi, "png", destFile);
        System.out.println("Input Encoded data is："+encodeddata);
    }
     
    /**
     * 解析二维码，返回解析内容
     * @param imageFile
     * @return
     */
    public static String qrCodeDecode(File imageFile) {
        String decodedData = null;
        QRCodeDecoder decoder = new QRCodeDecoder();
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
 
        try {
            decodedData = new String(decoder.decode(new J2SEImage(image)), "GBK");
            System.out.println("Output Decoded Data is："+decodedData);
        } catch (DecodingFailedException dfe) {
            System.out.println("Error: " + dfe.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedData;
    }
    
    /**
     * 编码字符串内容到目标httpResponse对象中
     * @param encodeddata
     * @param destFile
     * @throws IOException
     */
    public void qrCodeEncodeResponse(String encodeddata,HttpServletResponse response) throws IOException{
        Qrcode qrcode = new Qrcode();
        //错误修正容量   
        //L水平   7%的字码可被修正  
        //M水平   15%的字码可被修正  
        //Q水平   25%的字码可被修正  
        //H水平   30%的字码可被修正  
        //QR码有容错能力，QR码图形如果有破损，仍然可以被机器读取内容，最高可以到7%~30%面积破损仍可被读取。  
        //相对而言，容错率愈高，QR码图形面积愈大。所以一般折衷使用15%容错能力。
        qrcode.setQrcodeErrorCorrect('M');
        qrcode.setQrcodeEncodeMode('B');
        qrcode.setQrcodeVersion(7);
        byte[] d = encodeddata.getBytes("GBK");
        BufferedImage bi = new BufferedImage(139, 139, BufferedImage.TYPE_INT_RGB);
        // createGraphics
        Graphics2D g = bi.createGraphics();
        // set background
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, 139, 139);
        //设置二维码图片颜色
        g.setColor(Color.BLACK);
 
        if (d.length > 0 && d.length < 123) {
            boolean[][] b = qrcode.calQrcode(d);
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < b.length; j++) {
                    if (b[j][i]) {
                        g.fillRect(j * 3 + 2, i * 3 + 2, 3, 3);
                    }
                }
            }
        }
 
        g.dispose();
        bi.flush();
 
        ImageIO.write(bi, "png", response.getOutputStream());
        //System.out.println("Input Encoded data is："+encodeddata);
    }
    
    /**
     * 
     * createLoGoQRCode:(返回带logo的二维码). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     * TODO(这里描述这个方法的执行流程 – 可选).<br/>
     * TODO(这里描述这个方法的使用方法 – 可选).<br/>
     * TODO(这里描述这个方法的注意事项 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param content
     * @param imgPath
     * @param ccbPath
     */
    public static int createLoGoQRCode(String content, String imgPath,String ccbPath,int version) {    
        try {    
            Qrcode qrcodeHandler = new Qrcode();    
            //设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小    
            qrcodeHandler.setQrcodeErrorCorrect('M');    
            //N代表数字,A代表字符a-Z,B代表其他字符  
            qrcodeHandler.setQrcodeEncodeMode('B');   
            // 设置设置二维码版本，取值范围1-40，值越大尺寸越大，可存储的信息越大    
            qrcodeHandler.setQrcodeVersion(version);   
            // 图片尺寸    
            int imgSize =67 + 12 * (version - 1) ;  
    
            byte[] contentBytes = content.getBytes("gb2312");    
            BufferedImage image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);    
            Graphics2D gs = image.createGraphics();    
    
            gs.setBackground(Color.WHITE);    
            gs.clearRect(0, 0, imgSize, imgSize);    
    
            // 设定图像颜色 > BLACK    
            //gs.setColor(Color.BLUE);    
            gs.setColor(Color.BLACK);    
    
            // 设置偏移量 不设置可能导致解析出错    
            int pixoff = 2;    
            // 输出内容 > 二维码    
            if (contentBytes.length > 0 && contentBytes.length < 130) {  
                boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);  
                for (int i = 0; i < codeOut.length; i++) {  
                    for (int j = 0; j < codeOut.length; j++) {  
                        if (codeOut[j][i]) {  
                            gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);  
                        }  
                    }  
                }  
            } else {    
                System.err.println("QRCode content bytes length = "    
                        + contentBytes.length + " not in [ 0,125]. ");    
                return -1;  
            }    
            
            URL url = null;
            try {
            	url = new URL(ccbPath);
            	// 返回一个 URLConnection 对象，它表示到 URL 所引用的远程对象的连接。
                URLConnection uc = url.openConnection();
                // 打开的连接读取的输入流。
                InputStream in = uc.getInputStream();
			} catch (Exception e) {
				url = new URL(defaultQRLogo);
			}
            
            Image logo = ImageIO.read(url);
            //Image logo = ImageIO.read(new File(ccbPath));//实例化一个Image对象。  
            int widthLogo = logo.getWidth(null)>image.getWidth()*2/10?(image.getWidth()*2/10):logo.getWidth(null),   
                heightLogo = logo.getHeight(null)>image.getHeight()*2/10?(image.getHeight()*2/10):logo.getWidth(null);  
             
             /** 
               * logo放在中心 
              */  
            int x = (image.getWidth() - widthLogo) / 2;  
            int y = (image.getHeight() - heightLogo) / 2;  
            gs.drawImage(logo, x, y, widthLogo, heightLogo, null);  
            gs.dispose();    
            image.flush();    
    
            // 生成二维码QRCode图片    
            File imgFile = new File(imgPath);    
            ImageIO.write(image, "png", imgFile);    
    
        } catch (Exception e)   
        {    
            e.printStackTrace();    
            return -100;  
        }    
          
        return 0;  
    }
    
    /**
     * 
     * createLoGoQRCode:(返回带logo的二维码HttpResponse). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     * TODO(这里描述这个方法的执行流程 – 可选).<br/>
     * TODO(这里描述这个方法的使用方法 – 可选).<br/>
     * TODO(这里描述这个方法的注意事项 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param content
     * @param imgPath
     * @param ccbPath	图片的url
     */
    public static int createLoGoQRCodeHttpResponse(String content, String ccbPath,int version,HttpServletResponse response) {    
        try {    
            Qrcode qrcodeHandler = new Qrcode();    
            //设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小    
            qrcodeHandler.setQrcodeErrorCorrect('M');    
            //N代表数字,A代表字符a-Z,B代表其他字符  
            qrcodeHandler.setQrcodeEncodeMode('B');   
            // 设置设置二维码版本，取值范围1-40，值越大尺寸越大，可存储的信息越大    
            qrcodeHandler.setQrcodeVersion(version);   
            // 图片尺寸    
            int imgSize =67 + 12 * (version - 1) ;  
    
            byte[] contentBytes = content.getBytes("gb2312");    
            BufferedImage image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);    
            Graphics2D gs = image.createGraphics();    
    
            gs.setBackground(Color.WHITE);    
            gs.clearRect(0, 0, imgSize, imgSize);    
    
            // 设定图像颜色 > BLACK    
            //gs.setColor(Color.BLUE);    
            gs.setColor(Color.BLACK);    
    
            // 设置偏移量 不设置可能导致解析出错    
            int pixoff = 2;    
            // 输出内容 > 二维码    
            if (contentBytes.length > 0 && contentBytes.length < 130) {  
                boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);  
                for (int i = 0; i < codeOut.length; i++) {  
                    for (int j = 0; j < codeOut.length; j++) {  
                        if (codeOut[j][i]) {  
                            gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);  
                        }  
                    }  
                }  
            } else {    
                System.err.println("QRCode content bytes length = "    
                        + contentBytes.length + " not in [ 0,125]. ");    
                return -1;  
            }    
            
            URL url = null;
            try {
            	url = new URL(ccbPath);
            	// 返回一个 URLConnection 对象，它表示到 URL 所引用的远程对象的连接。
                URLConnection uc = url.openConnection();
                // 打开的连接读取的输入流。
                InputStream in = uc.getInputStream();
			} catch (Exception e) {
				url = new URL(defaultQRLogo);
			}
            
            Image logo = ImageIO.read(url);
            //Image logo = ImageIO.read(new File(ccbPath));//实例化一个Image对象。  
            int widthLogo = logo.getWidth(null)>image.getWidth()*2/10?(image.getWidth()*2/10):logo.getWidth(null),   
                heightLogo = logo.getHeight(null)>image.getHeight()*2/10?(image.getHeight()*2/10):logo.getWidth(null);  
             
             /** 
               * logo放在中心 
              */  
            int x = (image.getWidth() - widthLogo) / 2;  
            int y = (image.getHeight() - heightLogo) / 2;  
            gs.drawImage(logo, x, y, widthLogo, heightLogo, null);  
            gs.dispose();    
            image.flush();    
    
            // 生成二维码QRCode图片    
            /*File imgFile = new File(imgPath);    
            ImageIO.write(image, "png", imgFile);   */ 
            ImageIO.write(image, "png", response.getOutputStream());
    
        } catch (Exception e)   
        {    
            e.printStackTrace();    
            return -100;  
        }    
          
        return 0;  
    }
     
    public static void main(String[] args) {
        String filePath = "E:\\qr.png";
        /*File qrFile = new File(FilePath);
         
        //编码
        String encodeddata = "1111";
        try {
            QRCodeUtils.qrCodeEncode(encodeddata, qrFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };*/
         
        File qrFile = new File(filePath);
        //解码
        String reText = QRCodeUtils.qrCodeDecode(qrFile);
        System.out.println(reText);
        System.out.println("---------------------------");
        System.out.println(BCrypt.hashpw("123456", BCrypt.gensalt()));
    }
}
 
class J2SEImage implements QRCodeImage {
    BufferedImage image;
 
    public J2SEImage(BufferedImage image) {
        this.image = image;
    }
 
    public int getWidth() {
        return image.getWidth();
    }
 
    public int getHeight() {
        return image.getHeight();
    }
 
    public int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }
}

