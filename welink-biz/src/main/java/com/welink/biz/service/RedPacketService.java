package com.welink.biz.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.domain.RedPacket;
import com.welink.commons.domain.RedPacketResult;

/**
 * Created by daniel on 14-11-17.
 */
@Service(value = "redPacketService")
public class RedPacketService {

    //log
    private static org.slf4j.Logger log = LoggerFactory.getLogger(RedPacketService.class);

    
    //在之前的时间的比较
    public static String compare_date(String DATE1){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str="";
		Date dt1;
		try {
			dt1 = df.parse(DATE1);
			String newDate = df.format(new Date());
			Date dt3 = df.parse(newDate);
			long num=dt1.getTime()-dt3.getTime();
			//传入的时间一定要大于现在的时间才可以
			if(num>0){
				long day=num/(24*60*60*1000);
				long hour=(num/(60*60*1000)-day*24);
				long min=((num/(60*1000))-day*24*60-hour*60);
				str=""+day+"天"+hour+"小时"+min+"分";
			}else{
				str="-1";
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println(str);
		return str;
	}
    
    
    
  //通过时间进行获取对应的标示[1:在开始时间之前  2:在开始时间与结束时间之间  3:在结束时间之后]
  	public static int getTimeFlag(String beginTime,String endTime){
  		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		Date dt1;
  		Date dt2;
  		int flag=0;
  		try {
  			dt1 = df.parse(beginTime);
  			dt2 = df.parse(endTime);
  			String newDate = df.format(new Date());
  			Date dt3 = df.parse(newDate);
  			//当前时间-开始时间
  			long num1=dt3.getTime()-dt1.getTime();
  			System.out.println(num1);
  			//当前时间-结束时间
  			long num2=dt3.getTime()-dt2.getTime();
  			System.out.println(num2);
  			//传入的时间一定要大于现在的时间才可以
  			//在1的情况
  			if(num1<0 && num2<0){
  				flag=1;
  			}
  			//在开始与结束之间
  			else if(num1>=0 && num2<=0){
  				flag=2;
  			}
  			//在结束时间之后
  			else if(num1>0 &&num2>0){
  				flag=3;
  			}
  		} catch (ParseException e) {
  			e.printStackTrace();
  		}
  		return flag;
  	}
  	
  	
  	
  	
  	//判断是否中奖
  	public static boolean bingorp(Long percent){
		Random random=new Random();
		int s=random.nextInt(101);
		System.err.println(s);
		if(percent>=s){
			return true;
		}
		else{
			return false;
		}
	}
  	
  	
  	//中奖之后究竟拿哪一个红包，这里也是随机数来进行处理获取最终的数字
  	public static int getPrice(String onepackStr){
		String[] arr=onepackStr.split(",");
		int size=arr.length;
		Random random=new Random();
		int z=random.nextInt(size);
		int price=Integer.parseInt(arr[z]);
		return price;
	}
  	
  	
  	
  	
  	//进行字符串转时间戳
  	public static String getDateTimeSatampFromStr(String time){
  		String re_time = null;  
  		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
  		Date d;  
  		try {  
  			d = sdf.parse(time);  
  			long l = d.getTime();  
  			String str = String.valueOf(l);  
  			re_time = str;  
  		} catch (ParseException e) {  
  			e.printStackTrace();  
  		}  
  			return re_time;  
  	}
  	
  	//DoRedPacket
  	//最终接口的需要的json字符串
  	public static String rObjStr(RedPacket redPacket){
  		 WelinkVO welinkVO = new WelinkVO();
  		 Map map=new HashMap();
  		 map.put("beginTime",getDateTimeSatampFromStr(redPacket.getBeginTime()));
  		 map.put("endTime", getDateTimeSatampFromStr(redPacket.getEndTime()));
  		 map.put("num", redPacket.getNum());
  		 map.put("timeInfo", redPacket.getTimeInfo());
  		 map.put("info", redPacket.getInfo());
  		 map.put("flag", Long.parseLong(redPacket.getFlag()));
  		 welinkVO.setResult(map);
  		 welinkVO.setStatus(1);
  		 Long nowTime=(System.currentTimeMillis());
  		 map.put("nowTime", nowTime.toString());
		 System.out.println(nowTime.toString());
  		 System.err.println(JSON.toJSONString(welinkVO));
  		return JSON.toJSONString(welinkVO);
  	}
  	
  	
  	//DoRedPacketResult
  	public static String reresultObjStr(RedPacketResult redPacketResult){
  		 WelinkVO welinkVO = new WelinkVO();
  		 Map map=new HashMap();
  		 map.put("beginTime",getDateTimeSatampFromStr(redPacketResult.getBeginTime()));
  		 map.put("endTime", getDateTimeSatampFromStr(redPacketResult.getEndTime()));
  		 map.put("num", redPacketResult.getNum());
  		 map.put("timeInfo", redPacketResult.getTimeInfo());
  		 map.put("info", redPacketResult.getInfo());
  		 map.put("flag", Long.parseLong(redPacketResult.getFlag()));
  		 map.put("result", Long.parseLong(redPacketResult.getResult()));
  		 map.put("price", redPacketResult.getPrice());
  		 map.put("openid", redPacketResult.getOpenid());
  		 Long nowTime=(System.currentTimeMillis());
  		 map.put("nowTime", nowTime.toString());
  		 welinkVO.setResult(map);
  		 welinkVO.setStatus(1);
  		 System.err.println(JSON.toJSONString(welinkVO));
  		 return JSON.toJSONString(welinkVO);
  	}
  	
  	
  	
  	//进行数组进行字符串
  	public static String dolongarrToStr(long[] str){
  		String TRRstr="";
  		for(int i=0;i<str.length;i++){
  			if(i%10==0)System.out.println();
  			TRRstr+=(str[i]+"  ");
  		}
  		return TRRstr;
  	}
  	
  	
  	
  	//进行获取对应红包
  	/**
	 * 
	 * @param total
	 *            红包总额
	 * @param count
	 *            红包个数
	 * @param max
	 *            每个小红包的最大额
	 * @param min
	 *            每个小红包的最小额
	 * @return 存放生成的每个小红包的值的数组
	 */
	public static long[] generate(long total, int count, long max, long min) {
		long[] result = new long[count];

		long average = total / count;

		long a = average - min;
		long b = max - min;

		//
		//这样的随机数的概率实际改变了，产生大数的可能性要比产生小数的概率要小。
		//这样就实现了大部分红包的值在平均数附近。大红包和小红包比较少。
		long range1 = sqr(average - min);
		long range2 = sqr(max - average);

		for (int i = 0; i < result.length; i++) {
			//因为小红包的数量通常是要比大红包的数量要多的，因为这里的概率要调换过来。
			//当随机数>平均值，则产生小红包
			//当随机数<平均值，则产生大红包
			if (nextLong(min, max) > average) {
				// 在平均线上减钱
//				long temp = min + sqrt(nextLong(range1));
				long temp = min + xRandom(min, average);
				result[i] = temp;
				total -= temp;
			} else {
				// 在平均线上加钱
//				long temp = max - sqrt(nextLong(range2));
				long temp = max - xRandom(average, max);
				result[i] = temp;
				total -= temp;
			}
		}
		// 如果还有余钱，则尝试加到小红包里，如果加不进去，则尝试下一个。
		while (total > 0) {
			for (int i = 0; i < result.length; i++) {
				if (total > 0 && result[i] < max) {
					result[i]++;
					total--;
				}
			}
		}
		// 如果钱是负数了，还得从已生成的小红包中抽取回来
		while (total < 0) {
			for (int i = 0; i < result.length; i++) {
				if (total < 0 && result[i] > min) {
					result[i]--;
					total++;
				}
			}
		}
		return result;
	}
	
  	static Random random = new Random();
	static {
		random.setSeed(System.currentTimeMillis());
	}
	
	/**
	 * 生产min和max之间的随机数，但是概率不是平均的，从min到max方向概率逐渐加大。
	 * 先平方，然后产生一个平方值范围内的随机数，再开方，这样就产生了一种“膨胀”再“收缩”的效果。
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	static long xRandom(long min, long max) {
		return sqrt(nextLong(sqr(max - min)));
	}
	
	static long sqrt(long n) {
		return (long) Math.sqrt(n);
	}

	static long sqr(long n) {
		return n * n;
	}
	
	static long nextLong(long n) {
		return random.nextInt((int) n);
	}

	static long nextLong(long min, long max) {
		return random.nextInt((int) (max - min + 1)) + min;
	}
}
