package com.welink.web.httpclient;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easemob.server.example.api.IMUserAPI;
import com.easemob.server.example.comm.ClientContext;
import com.easemob.server.example.comm.EasemobRestAPIFactory;
import com.easemob.server.example.comm.body.IMUserBody;
import com.easemob.server.example.comm.body.IMUsersBody;
import com.easemob.server.example.comm.wrapper.BodyWrapper;
import com.easemob.server.example.comm.wrapper.ResponseWrapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.welink.biz.common.security.Md5;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("test")
public class EasemobRestTest {
	
	@Resource
	private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	private static final JsonNodeFactory factory = new JsonNodeFactory(false);
	
	/**
	 * 创建专家
	 * @throws Exception
	 */
	@Test
    public void createNewIMUserBatch() throws Exception {
		MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
    	//mikuCsadDOExample.createCriteria().andEmUserNameIsNull();
    	List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
    	if(!mikuCsadDOList.isEmpty()){
    		//创建多个用户
    		EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();
    		
    		IMUserAPI user = (IMUserAPI)factory.newInstance(EasemobRestAPIFactory.USER_CLASS);
    		
    		List<IMUserBody> users = new ArrayList<IMUserBody>();
    		for(MikuCsadDO mikuCsadDO : mikuCsadDOList){
    			ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(mikuCsadDO.getUserId());
    			if(null != profileDO && null == profileDO.getEmUserName()){
    				String emchatPw = Md5.MD5Encode(BizConstants.EMCHAT_PW+mikuCsadDO.getUserId());
    				System.out.println("-emchatPw--------------------: "+emchatPw);
    				users.add(new IMUserBody(BizConstants.EMCHAT_CUSTOMER_PRE+mikuCsadDO.getUserId(), emchatPw, mikuCsadDO.getCsadName()));
    				profileDO.setEmUserName(BizConstants.EMCHAT_CUSTOMER_PRE+mikuCsadDO.getUserId());
    				profileDO.setEmUserPw(emchatPw);
    				profileDOMapper.updateByPrimaryKeySelective(profileDO);
    			}
    		}
    		BodyWrapper usersBody = new IMUsersBody(users);
    		Object createNewIMUserBatch = user.createNewIMUserBatch(usersBody);
    		System.out.println("------------------------createNewIMUserBatch--------------------------------------------");
    		System.out.println(JSON.toJSONString(createNewIMUserBatch));
    	}
		
    }

	public static void main(String[] args) throws Exception {
		Properties p = new Properties();

		try {
			InputStream inputStream = ClientContext.class.getClassLoader().getResourceAsStream("configEmchat.properties");
			p.load(inputStream);
		} catch (IOException e) {
			return; // Context not initialized
		}
		
		String app = p.getProperty("API_APP");
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("app-----------------"+app);
		
		
		EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();
		
		IMUserAPI user = (IMUserAPI)factory.newInstance(EasemobRestAPIFactory.USER_CLASS);
		/*ChatMessageAPI chat = (ChatMessageAPI)factory.newInstance(EasemobRestAPIFactory.MESSAGE_CLASS);
		FileAPI file = (FileAPI)factory.newInstance(EasemobRestAPIFactory.FILE_CLASS);
		SendMessageAPI message = (SendMessageAPI)factory.newInstance(EasemobRestAPIFactory.SEND_MESSAGE_CLASS);
		ChatGroupAPI chatgroup = (ChatGroupAPI)factory.newInstance(EasemobRestAPIFactory.CHATGROUP_CLASS);
		ChatRoomAPI chatroom = (ChatRoomAPI)factory.newInstance(EasemobRestAPIFactory.CHATROOM_CLASS);*/

        /*ResponseWrapper fileResponse = (ResponseWrapper) file.uploadFile(new File("d:/logo.png"));
        String uuid = ((ObjectNode) fileResponse.getResponseBody()).get("entities").get(0).get("uuid").asText();
        String shareSecret = ((ObjectNode) fileResponse.getResponseBody()).get("entities").get(0).get("share-secret").asText();
        InputStream in = (InputStream) ((ResponseWrapper) file.downloadFile(uuid, shareSecret, false)).getResponseBody();
        FileOutputStream fos = new FileOutputStream("d:/logo1.png");
        byte[] buffer = new byte[1024];
        int len1 = 0;
        while ((len1 = in.read(buffer)) != -1) {
            fos.write(buffer, 0, len1);
        }
        fos.close();*/

		//发送消息
		/*String targetType = "users";
		List<String> list = new ArrayList<String>();  
		list.add("expert_70468");  
		int size = list.size();  
		String[] targetusers = (String[])list.toArray(new String[size]);
		String from = "customer_78887";
		Map<String, String> ext = new HashMap<String, String>();
		String msg = "hello.....4444";
		TextMessageBody textMessageBody = new TextMessageBody(targetType, targetusers, from, ext, msg);
		Object sendMessage = message.sendMessage(textMessageBody);
		System.out.println("------------sendMessage---------------------------------------------: ");
		System.out.println(JSON.toJSONString(sendMessage));
		
		String currentTimestamp = String.valueOf(System.currentTimeMillis());
        String senvenDayAgo = String.valueOf(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
		Object exportChatMessages = chat.exportChatMessages(10L, null,  "select * where timestamp<" + currentTimestamp);
		System.out.println("------------exportChatMessages---------------------------------------------: ");
		System.out.println(JSON.toJSONString(exportChatMessages));*/
		
		
		// Create a IM user
		/*String md5pw = Md5.MD5Encode("123456");
		BodyWrapper userBody = new IMUserBody("test_lgc003", "123456", "test_lgc0003");
		ResponseWrapper createNewIMUserSingle = (ResponseWrapper) user.createNewIMUserSingle(userBody);
		System.out.println("------------createNewIMUserSingle---------------------------------------------: "+createNewIMUserSingle.getResponseStatus());
		System.out.println(JSON.toJSONString(createNewIMUserSingle));*/

		// Create some IM users
		/*List<IMUserBody> users = new ArrayList<IMUserBody>();
		users.add(new IMUserBody("User002", "123456", null));
		users.add(new IMUserBody("User003", "123456", null));
		BodyWrapper usersBody = new IMUsersBody(users);
		user.createNewIMUserBatch(usersBody);*/
		
		// Get a IM user
		/*user.getIMUsersByUserName("User001");
		
		// Get a fake user
		user.getIMUsersByUserName("FakeUser001");
		
		// Get 12 users
		user.getIMUsersBatch(null, null);*/
		
		/*Object imUsersByUserName = user.getIMUsersByUserName("customer_71319");
		System.out.println("------------------------imUsersByUserName--------------------------------");
		System.out.println(JSON.toJSONString(imUsersByUserName));*/
		
		
		
		ResponseWrapper imUserStatus = (ResponseWrapper)user.getIMUserStatus("chat_78932");
		
		//Object imUsersByUserName = user.getIMUsersByUserName("customer_71319");
		System.out.println("--------------------------------------");
		System.out.println("-----11:"+imUserStatus.getResponseStatus());
		System.out.println("-----22:"+imUserStatus.toString());
		JSONObject parseObject = JSON.parseObject(String.valueOf(imUserStatus.getResponseBody()));
		Object object = parseObject.get("data");
		System.out.println(object);
	}
	

}
