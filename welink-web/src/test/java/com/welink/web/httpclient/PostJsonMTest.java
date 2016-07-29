package com.welink.web.httpclient;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

public class PostJsonMTest {

	/*@Before
    public void before(){                                                                    
    }*/
     
    @Test
    public void login_json(){
    	// TODO Auto-generated method stub
		HttpClient client = new DefaultHttpClient();
		//HttpPost post = new HttpPost("http://localhost:8888/unes_mikuagent_interface/system/merchants/getSelectMerchants");
		HttpPost post = new HttpPost("http://localhost:8080/welink/api/m/1.0/login.json");
	    /*JSONObject obj = new JSONObject();
	    obj.put("menu.id",Long.valueOf("2"));
	    obj.put("menu.menuName", "menu2");
	    obj.put("menu.url", "url2");*/
		
		ObjectMapper mapper = new ObjectMapper();  
		//m=18258168813&p=123456&hp=123&ip=1&deviceId=1
		//创建根节点  
        ObjectNode root = mapper.createObjectNode();  
        root.put("m", "15622395287");
        root.put("p", "123456");
        root.put("hp", "123456");
        root.put("deviceId", "lgcdeviceId");
		
		/*JSONObject obj = new JSONObject();
	    obj.put("username", "18922702208");
	    obj.put("mobile", "18922702208");*/
		
		//String obj = "{\"menu\":{\"id\":\"1\", \"menuName\":\"menu2\"}, \"url\":\"url2\"}}";
		
	    //post.setEntity(new StringEntity(nameValuePairs.toString());
	   // StringEntity se = new StringEntity(obj.toString(),"UTF-8");
	    try {
	    	StringEntity se = new StringEntity( mapper.writeValueAsString(root),"UTF-8");
	    	se.setContentEncoding("UTF-8");
	    	se.setContentType("application/json");
	    	post.setEntity(se);
			HttpResponse response = client.execute(post);
			System.out.println("------------------------------------------------------");
			System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    @Test
    public void itemSearchSuggest(){
    	DefaultHttpClient client = new DefaultHttpClient();
    	//HttpClient client = new DefaultHttpClient();
	    //HttpPost post = new HttpPost("http://localhost:8888/unes_mikuagent_interface/merchants/login");
    	//HttpPost post = new HttpPost("http://localhost:8181/api/m/1.0/itemSearchSuggest.json");
    	HttpPost post = new HttpPost("http://localhost:8181/api/m/1.0/confirmOrder.json");
	    
	    NameValuePair nameValuePair = new BasicNameValuePair("items",
	    		"[{\"item_id\":8057, \"num\":2}]");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(nameValuePair);
	    
	    UrlEncodedFormEntity encodedHE;
		try {
			encodedHE = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
			post.setEntity(encodedHE);
			HttpResponse response = client.execute(post);
			/*Header[] allHeaders = response.getAllHeaders();
			for(Header head : allHeaders){
				System.out.println("head...........: "+head.getName()+": "+head.getValue());
			}*/
			//获取cookie
			/*List<Cookie> cookies = client.getCookieStore().getCookies();
		    if (cookies.isEmpty()) {
		        System.out.println("No cookies");
		    } else{
		        for (Cookie c : cookies){
		            System.out.println("-" + c.toString());
		        }
		    }*/
			
			System.out.println("------------------------------------------------------");
			System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void login(){
    	DefaultHttpClient client = new DefaultHttpClient();
    	//HttpClient client = new DefaultHttpClient();
	    //HttpPost post = new HttpPost("http://localhost:8888/unes_mikuagent_interface/merchants/login");
    	HttpPost post = new HttpPost("http://localhost:8080/welink/api/m/1.0/login.json");
	    
	    NameValuePair nameValuePair = new BasicNameValuePair("m",
	    		"15622395287");
	    NameValuePair nameValuePair2 = new BasicNameValuePair("p",
	    		"123456");
	    NameValuePair nameValuePair3 = new BasicNameValuePair("hp",
	    		"123456");
	    NameValuePair nameValuePair4 = new BasicNameValuePair("deviceId",
	    		"lgcdeviceId");
	    NameValuePair nameValuePair5 = new BasicNameValuePair("sarah",
	    		"sarah");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(nameValuePair);
	    nameValuePairs.add(nameValuePair2);
	    nameValuePairs.add(nameValuePair3);
	    nameValuePairs.add(nameValuePair4);
	    nameValuePairs.add(nameValuePair5);
	    
	    UrlEncodedFormEntity encodedHE;
		try {
			encodedHE = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
			post.setEntity(encodedHE);
			HttpResponse response = client.execute(post);
			/*Header[] allHeaders = response.getAllHeaders();
			for(Header head : allHeaders){
				System.out.println("head...........: "+head.getName()+": "+head.getValue());
			}*/
			//获取cookie
			/*List<Cookie> cookies = client.getCookieStore().getCookies();
		    if (cookies.isEmpty()) {
		        System.out.println("No cookies");
		    } else{
		        for (Cookie c : cookies){
		            System.out.println("-" + c.toString());
		        }
		    }*/
			
			System.out.println("------------------------------------------------------");
			System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void regist(){
    	DefaultHttpClient client = new DefaultHttpClient();
    	//HttpClient client = new DefaultHttpClient();
	    //HttpPost post = new HttpPost("http://localhost:8888/unes_mikuagent_interface/merchants/login");
    	HttpPost post = new HttpPost("http://localhost:8080/welink/api/m/1.0/register.json");
	    
	    NameValuePair nameValuePair = new BasicNameValuePair("mobile",
	    		"15622395287");
	    NameValuePair nameValuePair2 = new BasicNameValuePair("pswd",
	    		"123456");
	    NameValuePair nameValuePair3 = new BasicNameValuePair("hp",
	    		"123456");
	    NameValuePair nameValuePair4 = new BasicNameValuePair("checkNum",
	    		"1");
	    NameValuePair nameValuePair5 = new BasicNameValuePair("ip",
	    		"192.168.1.45");
	    NameValuePair nameValuePair6 = new BasicNameValuePair("deviceId",
	    		"lgcdeviceId");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(nameValuePair);
	    nameValuePairs.add(nameValuePair2);
	    nameValuePairs.add(nameValuePair3);
	    nameValuePairs.add(nameValuePair4);
	    nameValuePairs.add(nameValuePair5);
	    nameValuePairs.add(nameValuePair6);
	    
	    UrlEncodedFormEntity encodedHE;
		try {
			encodedHE = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
			post.setEntity(encodedHE);
			HttpResponse response = client.execute(post);
			/*Header[] allHeaders = response.getAllHeaders();
			for(Header head : allHeaders){
				System.out.println("head...........: "+head.getName()+": "+head.getValue());
			}*/
			//获取cookie
			/*List<Cookie> cookies = client.getCookieStore().getCookies();
		    if (cookies.isEmpty()) {
		        System.out.println("No cookies");
		    } else{
		        for (Cookie c : cookies){
		            System.out.println("-" + c.toString());
		        }
		    }*/
			
			System.out.println("------------------------------------------------------");
			System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}
