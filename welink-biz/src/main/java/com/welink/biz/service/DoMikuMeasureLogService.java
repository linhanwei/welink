package com.welink.biz.service;

import groovy.json.internal.Byt;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.welink.biz.common.model.ItemViewDO;
import com.welink.commons.domain.InstallActiveDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuInstrumentMeasureLogDO;
import com.welink.commons.domain.MikuInstrumentMeasureLogDOExample;
import com.welink.commons.domain.MikuMineSkincareSuggestion;
import com.welink.commons.domain.MikuMineSkincareSuggestionExample;
import com.welink.commons.domain.MikuOperMeaureData;
import com.welink.commons.domain.MikuOperMeaureDetail;
import com.welink.commons.domain.MikuOperMeaureListData;
import com.welink.commons.domain.WeChatProfileDO;
import com.welink.commons.domain.WeChatProfileDOExample;
import com.welink.commons.persistence.InstallActiveDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuInstrumentMeasureLogDOMapper;
import com.welink.commons.persistence.MikuMineSkincareSuggestionMapper;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by daniel on 15-3-18.
 */
@Service
public class DoMikuMeasureLogService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(DoMikuMeasureLogService.class);
    
    @Resource
	private MikuInstrumentMeasureLogDOMapper mikuInstrumentMeasureLogDOMapper;
    @Resource
    private MikuMineSkincareSuggestionMapper mikuMineSkincareSuggestionMapper;
    @Resource
    private ItemMapper itemMapper;
    
    
    
   //根据id查找分析对应的记录
    public Map getOperateDataById(Long id){
    	Map finalmap=new HashMap();
    	//获取的是对应记录信息
    	MikuInstrumentMeasureLogDO m=getOneData(id);
    	Map map=getAlldataByType();
    	List<MikuMineSkincareSuggestion> dataList=new ArrayList<MikuMineSkincareSuggestion>();
    	//水分
    	dataList.add(getResult(map,"1",m.getMoistureValue()));
    	//油分
    	dataList.add(getResult(map,"2",m.getOilValue()));
    	//弹性
    	dataList.add(getResult(map,"3",m.getResilienceValue()));
    	//衰老
    	dataList.add(getResult(map,"4",m.getSenilityValue()));
    	finalmap.put("numdatalist", dataList);
    	finalmap.put("onedata", m);
//    	List<Item> itemlist= getItemList();
    	finalmap.put("itemlist", getItemList());
    	//还有一个商品集合列表没有添加
    	return finalmap;
    }
    
    
    //根据对应类型来进行获取对应的数据：算出对应的区间
    public MikuMineSkincareSuggestion getResult(Map map,String flag,BigDecimal num){
    	List<MikuMineSkincareSuggestion> list=(List<MikuMineSkincareSuggestion>) map.get(flag);
    	MikuMineSkincareSuggestion mikuMineSkincareSuggestion=new MikuMineSkincareSuggestion();
    	for(int z=0;z<list.size();z++){
    		MikuMineSkincareSuggestion data=list.get(z);
    		//获取的是区间值
    		String range=data.getMeasureItemLevelRange();
    		//获取的2个值
    		String[] arr=range.split(":");
    		BigDecimal begin=new BigDecimal(arr[0]);
    		BigDecimal end=new BigDecimal(arr[1]);
    		if(num.compareTo(begin)>=0 && end.compareTo(num)>=0){
    			mikuMineSkincareSuggestion=data;
    		}
    	}
    	return mikuMineSkincareSuggestion;
    }
    
    
    
    
    
    
    //根据各类型进行分类
    public Map getAlldataByType(){
    	Map map=new HashMap();
    	List<MikuMineSkincareSuggestion> list= getAllListdata();
    	for(int i=1;i<5;i++){
    		List<MikuMineSkincareSuggestion> onedata=new ArrayList<MikuMineSkincareSuggestion>();
    		for(int j=0;j<list.size();j++){
    			MikuMineSkincareSuggestion data=list.get(j);
    			if((byte)i==data.getMeasureItem()){
    				onedata.add(data);
    			}
    		}
    		map.put(i+"", onedata);
    	}
    	return map;
    }
    
    
    
    
    
    
    
    
  
    
    //测试数据
    public MikuInstrumentMeasureLogDO insertOneDataByparamsTest(String measureValue, String moistureValue,
			String oilValue, String resilienceValue, String senilityValue,
			long profileId, Byte measureType, Byte instrumentType,Byte testPositon,int year,int month,int day,int hour) {
		MikuInstrumentMeasureLogDO measureLogDO=new MikuInstrumentMeasureLogDO();
		measureLogDO.setMeasureType(measureType);
		measureLogDO.setInstrumentType(instrumentType);
		measureLogDO.setMeasureValue(new BigDecimal(measureValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setMoistureValue(new BigDecimal(moistureValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setOilValue(new BigDecimal(oilValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setResilienceValue(new BigDecimal(resilienceValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setSenilityValue(new BigDecimal(senilityValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setDateCreated(new Date());
		measureLogDO.setLastUpdated(new Date());
		measureLogDO.setUserId(profileId);
		//进行年月的修改
		Calendar cal = Calendar.getInstance();
        int week=cal.get(Calendar.WEEK_OF_MONTH);//周
        measureLogDO.setCreateYear(year);
        measureLogDO.setCreateMonth(month);
        measureLogDO.setCreateDay(day);
        measureLogDO.setCreateWeek(week);
        measureLogDO.setCreateHour(hour);
        //基本
        measureLogDO.setVersion(0L);
        measureLogDO.setTestPosition(testPositon);
        mikuInstrumentMeasureLogDOMapper.insert(measureLogDO);
		return measureLogDO;
	}
    
    
    
//   SELECT 
//	 SUM(measure_value) AS  measure_value,SUM(moisture_value)  AS  moisture_value,
//	 SUM(oil_value) AS  oil_value,SUM(resilience_value)  AS  resilience_value,SUM(senility_value) AS  senility_value,
//	 test_position
//   FROM miku_instrument_measure_log 
//   WHERE create_year=2016 AND create_month=3 AND user_id=1
//   GROUP BY test_position
   
	public MikuInstrumentMeasureLogDO insertOneDataByparams(String measureValue, String moistureValue,
			String oilValue, String resilienceValue, String senilityValue,
			long profileId, Byte measureType, Byte instrumentType,Byte testPositon) {
		MikuInstrumentMeasureLogDO measureLogDO=new MikuInstrumentMeasureLogDO();
		measureLogDO.setMeasureType(measureType);
		measureLogDO.setInstrumentType(instrumentType);
		measureLogDO.setMeasureValue(new BigDecimal(measureValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setMoistureValue(new BigDecimal(moistureValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setOilValue(new BigDecimal(oilValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setResilienceValue(new BigDecimal(resilienceValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setSenilityValue(new BigDecimal(senilityValue).setScale(2,  BigDecimal.ROUND_HALF_UP));
		measureLogDO.setDateCreated(new Date());
		measureLogDO.setLastUpdated(new Date());
		measureLogDO.setUserId(profileId);
		//进行年月的修改
		Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);//获取年份
        int month=cal.get(Calendar.MONTH)+1;//获取月份
        int day=cal.get(Calendar.DATE);//获取日
        int hour=cal.get(Calendar.HOUR_OF_DAY);//小时
        int week=cal.get(Calendar.WEEK_OF_MONTH);//周
        measureLogDO.setCreateYear(year);
        measureLogDO.setCreateMonth(month);
        measureLogDO.setCreateDay(day);
        measureLogDO.setCreateWeek(week);
        measureLogDO.setCreateHour(hour);
        //基本
        measureLogDO.setVersion(0L);
        measureLogDO.setTestPosition(testPositon);
        mikuInstrumentMeasureLogDOMapper.insert(measureLogDO);
		return measureLogDO;
	}
//    SELECT 
//	  SUM(measure_value) AS  measure_value,SUM(moisture_value)  AS  moisture_value,
//	  SUM(oil_value) AS  oil_value,SUM(resilience_value)  AS  resilience_value,SUM(senility_value) AS  senility_value,
//	  test_position,create_hour
//   FROM miku_instrument_measure_log 
//   WHERE create_year=2016 AND  create_month=3  AND create_day=19 AND user_id=1
//   GROUP BY test_position,create_hour
	
	
	
	public List<MikuOperMeaureListData> getOperaterListByParams(Long userId,String timeType){
		List<MikuOperMeaureListData> list=new ArrayList<MikuOperMeaureListData>();
		MikuOperMeaureListData mikOperMeaureListData=new MikuOperMeaureListData();
		Map<String,Object> map=new HashMap<String,Object>();
		switch (timeType) {
		case "Y":
			Calendar cal = Calendar.getInstance();
	        int year = cal.get(Calendar.YEAR);//获取年份
	        int month=cal.get(Calendar.MONTH)+1;//获取月份
	        List<String> strList= getNextMonth(year,month);
	        list=getEveryMonth(strList,userId);
			break;
		case "M":
			for(int i=29;i>=0;i--){
				list.add(getOneMikuOperMeaureListData(i,userId));
			}		
			break;
		case "W":
			for(int i=6;i>=0;i--){
				list.add(getOneMikuOperMeaureListData(i,userId));
			}
			break;
		case "D":
			list.add(getOneMikuOperMeaureListData(0,userId));
			break;
		case "H":
			MikuOperMeaureListData one=new MikuOperMeaureListData();
			Map<String,Object> hourmap=getTypeParams(0,userId);
			List<MikuOperMeaureData> onelist=mikuInstrumentMeasureLogDOMapper.selectByHourParamsByAvg(hourmap);
			one.setList(onelist);
			one.setTime((String) hourmap.get("time"));
			list.add(one);
			break;
		default:
			break;
		}
		
		return list;
	}
	
	
	
	
	
	
	
	
	
	//这是求平均值:4个部位平均值的排序
	public List<MikuOperMeaureData> getOperaterListByAvgParams(Long userId,String timeType){
		List<MikuOperMeaureData> list=new ArrayList<MikuOperMeaureData>();
		List<MikuOperMeaureListData>  oplist=getOperaterListByParams(userId,timeType);
		switch (timeType) {
		case "Y":
		case "M":
		case "W":
		case "D":
			list=getAvgData(oplist);
			break;
		case "H":
			break;
		default:
			break;
		}
		return list;
	}
	
	
	//传参数进行求平均值
//	CAST(AVG(measure_value) AS DECIMAL(5,1)) as  measure_value,
//	CAST(AVG(moisture_value) AS DECIMAL(5,1)) as  moisture_value,
//	CAST(AVG(oil_value) AS DECIMAL(5,1)) as  oil_value,
//	CAST(AVG(resilience_value) AS DECIMAL(5,1))  as  resilience_value,
//	CAST(AVG(senility_value) AS DECIMAL(5,1)) as  senility_value,
	public List<MikuOperMeaureData> getAvgData(List<MikuOperMeaureListData> list){
		List<MikuOperMeaureData> nlist=new ArrayList<MikuOperMeaureData>();
		for(int k=1;k<5;k++){
			MikuOperMeaureData m=new MikuOperMeaureData();
			BigDecimal measureV=new BigDecimal(0);
			BigDecimal oilV=new BigDecimal(0);
			BigDecimal resilienceV=new BigDecimal(0);
			BigDecimal senilityV=new BigDecimal(0);
			BigDecimal moistureV=new BigDecimal(0);
			int size=0;
			for(int i=0;i<list.size();i++){
				List<MikuOperMeaureData> oplist=list.get(i).getList();
				for(int z=0;z<oplist.size();z++){
					MikuOperMeaureData mkopdata=oplist.get(z);
					if((byte)k == mkopdata.getTestPosition()){
						//相加
						size++;
						measureV=measureV.add(mkopdata.getMeasureValue());
						oilV=oilV.add(mkopdata.getOilValue());
						resilienceV=resilienceV.add(mkopdata.getResilienceValue());
						senilityV=senilityV.add(mkopdata.getSenilityValue());
						moistureV=moistureV.add(mkopdata.getMoistureValue());
					}
				}
			}
			
			m.setTestPosition((byte)k);
			
			if(size>0){
				BigDecimal newmeasureV=measureV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newOilV=oilV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newResilienceV=resilienceV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newSenilityV=senilityV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newMoistureV=moistureV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				m.setMeasureValue(newmeasureV);
				m.setOilValue(newOilV);
				m.setResilienceValue(newResilienceV);
				m.setSenilityValue(newSenilityV);
				m.setMoistureValue(newMoistureV);
			}else{
				m.setMeasureValue(measureV);
				m.setOilValue(oilV);
				m.setResilienceValue(resilienceV);
				m.setSenilityValue(senilityV);
				m.setMoistureValue(moistureV);
			}
			
			nlist.add(m);
		}
		return nlist;
	}
	
	
	
	
	
	
	
	
	//获取详情信息：时间  各个指标的值   横坐标   一个总值
	public List<MikuOperMeaureDetail> getAllListData(Long userId,String timeType,Byte testPosition){
		List<MikuOperMeaureDetail> newoplist=new ArrayList<MikuOperMeaureDetail>();
		List<MikuOperMeaureData> list=new ArrayList<MikuOperMeaureData>();
		List<MikuOperMeaureListData>  oplist=getOperaterListByParams(userId,timeType);
		MikuOperMeaureData avgposi=getOneMikuOperateDataBytestPosition(userId,timeType,testPosition);
		list=getAvgData(oplist);
		switch (timeType) {
		case "Y":
		case "M":
		case "W":
			newoplist=getDWYData(oplist,timeType,testPosition,avgposi);
			break;
		case "D":
			oplist=getOperaterListByParams(userId,"H");
			//里面数据都是当天的数据
			MikuOperMeaureListData oneday=oplist.get(0);
			newoplist=getHourData(oneday,testPosition);
			newoplist=addSumParams(newoplist,getOperaterListByAvgParams(userId,timeType),avgposi);
			break;
		default:
			break;
		}
		return newoplist;
	}
	
	
	//根据部位来获取平均值
	public MikuOperMeaureData getOneMikuOperateDataBytestPosition(Long userId,String timeType,Byte testPosition){
		//平均值
//		List<MikuOperMeaureData> avgList=getOperaterListByAvgParams(userId,timeType);
//		getNewDataListByParams
		List<MikuOperMeaureData> avgList=getNewDataListByParams(userId,timeType);
		MikuOperMeaureData meaureData=new MikuOperMeaureData();
		for(int i=0;i<avgList.size();i++){
			MikuOperMeaureData one=avgList.get(i);
			if(testPosition == one.getTestPosition()){
				meaureData=one;
			}
		}
		return meaureData;
	}
	
	
	
	
	//具体综合的添加
	public List<MikuOperMeaureDetail> addSumParams(List<MikuOperMeaureDetail> one,List<MikuOperMeaureData>  oplist,MikuOperMeaureData avgposi){
		for(int z=0;z<one.size();z++){
			MikuOperMeaureDetail data=one.get(z);
//			mdetail.setSumCount(new BigDecimal(0));
			data.setSumCount(new BigDecimal(0));
			Byte type=data.getType();
			for(int i=0;i<oplist.size();i++){
//				if(type==oplist.get(i).getTestPosition()){
					switch (type) {
					case 1:
//						data.setSumCount(oplist.get(i).getMeasureValue());
						data.setSumCount(avgposi.getMeasureValue());
						break;
					case 2:
//						data.setSumCount(oplist.get(i).getMoistureValue());
						data.setSumCount(avgposi.getMoistureValue());
						break;
					case 3:
//						data.setSumCount(oplist.get(i).getOilValue());
						data.setSumCount(avgposi.getOilValue());
						break;
					case 4:
//						data.setSumCount(oplist.get(i).getResilienceValue());
						data.setSumCount(avgposi.getResilienceValue());
						break;
					case 5:
//						data.setSumCount(oplist.get(i).getSenilityValue());
						data.setSumCount(avgposi.getSenilityValue());
						break;
					default:
						break;
					}
//				}
			}	
		}
		return one;
	}
	
	
	//拿到全部的数据进行操作
	public List<MikuOperMeaureDetail> getDWYData(List<MikuOperMeaureListData>  oplist,String timetype,Byte testPosition,MikuOperMeaureData avgposi){
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		//时间的坐标
		String str=df.format(new Date());
		List<MikuOperMeaureDetail> list=new ArrayList<MikuOperMeaureDetail>();
		List<MikuOperMeaureData> alllist=new ArrayList<MikuOperMeaureData>();
		for(int i=0;i<oplist.size();i++){
			MikuOperMeaureListData mm=oplist.get(i);
			List<MikuOperMeaureData> opthrlist=mm.getList();
			alllist.addAll(opthrlist);
		}
		//获取的是时间的集合
		List<String> timeList=getTimeList(timetype);
		for(int z=1;z<6;z++){
				MikuOperMeaureDetail mdetail=new MikuOperMeaureDetail();
				mdetail.setTimetype(str);
				mdetail.setType((byte) z);
				List<MikuOperMeaureData> opmeauredata=new ArrayList<MikuOperMeaureData>();
				for(int j=0;j<timeList.size();j++){
					String jtstr=timeList.get(j);
					MikuOperMeaureData typedata=new MikuOperMeaureData();
					typedata.setTimetype(jtstr);
					BigDecimal count=new BigDecimal(0);
					int cout=0;
					for(int m=0;m<alllist.size();m++){
						MikuOperMeaureData data=alllist.get(m);
						if(jtstr.equals(data.getTimetype()) && (testPosition == data.getTestPosition())){
							cout++;
							switch (z) {
							case 1:
								count=count.add(data.getMeasureValue());
								break;
							case 2:
								count=count.add(data.getMoistureValue());
								break;
							case 3:
								count=count.add(data.getOilValue());
								break;
							case 4:
								count=count.add(data.getResilienceValue());
								break;
							case 5:
								count=count.add(data.getSenilityValue());
								break;
							default:
								break;
							}
						}
					}
					typedata.setContext(count);
					opmeauredata.add(typedata);
				}
				mdetail.setList(opmeauredata);
				mdetail.setSumCount(new BigDecimal(0));
				switch (z) {
				case 1:
					mdetail.setSumCount(avgposi.getMeasureValue());
					break;
				case 2:
					mdetail.setSumCount(avgposi.getMoistureValue());
					break;
				case 3:
					mdetail.setSumCount(avgposi.getOilValue());
					break;
				case 4:
					mdetail.setSumCount(avgposi.getResilienceValue());
					break;
				case 5:
					mdetail.setSumCount(avgposi.getSenilityValue());
					break;
				default:
					break;
				}
				list.add(mdetail);
		}
		return list;
	}
	
	
	
	//详情内部的时间的集合
	public List<String> getTimeList(String timetype){
		List<String> list=new ArrayList<String>();
		switch (timetype) {
		case "Y":
			Calendar cal = Calendar.getInstance();
	        int year = cal.get(Calendar.YEAR);//获取年份
	        int month=cal.get(Calendar.MONTH)+1;//获取月份
	        list= getNextMonth(year,month);
			break;
		case "M":
			for(int i=29;i>=0;i--){
				Map<String,Object> map=new HashMap<String,Object>();
				map=getTypeParams(i,1L);
				list.add((String) map.get("time"));
			}
			
			break;
		case "W":
			for(int i=6;i>=0;i--){
				Map<String,Object> map=new HashMap<String,Object>();
				map=getTypeParams(i,1L);
				list.add((String) map.get("time"));
			}
			break;

		default:
			break;
		}
		return list;
	} 
	
	
	
	
	
	
	
	
	
	
	//单独对时间的获取值操作
	public List<MikuOperMeaureDetail> getHourData(MikuOperMeaureListData oneday,Byte testPosition){
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		//时间的坐标
		String str=df.format(new Date());
		List<MikuOperMeaureDetail> list=new ArrayList<MikuOperMeaureDetail>();
		List<MikuOperMeaureData> oplist=oneday.getList();
		for(int z=1;z<6;z++){
				MikuOperMeaureDetail mdetail=new MikuOperMeaureDetail();
				mdetail.setTimetype(str);
				mdetail.setType((byte) z);
				List<MikuOperMeaureData> opmeauredata=new ArrayList<MikuOperMeaureData>();
				//统计的是24个小时数据
				for(int j=0;j<24;j++){
					MikuOperMeaureData typedata=new MikuOperMeaureData();
					typedata.setTimetype(j+":00");
					BigDecimal count=new BigDecimal(0);
					int cout=0;
					for(int m=0;m<oplist.size();m++){
						MikuOperMeaureData data=oplist.get(m);
						if((""+j).equals(data.getTimetype()) && (testPosition == data.getTestPosition())){
							cout++;
							switch (z) {
							case 1:
								count=count.add(data.getMeasureValue());
								break;
							case 2:
								count=count.add(data.getMoistureValue());
								break;
							case 3:
								count=count.add(data.getOilValue());
								break;
							case 4:
								count=count.add(data.getResilienceValue());
								break;
							case 5:
								count=count.add(data.getSenilityValue());
								break;
							default:
								break;
							}
						}
					}
					typedata.setContext(count);
					opmeauredata.add(typedata);
				}
				mdetail.setList(opmeauredata);
				list.add(mdetail);
		}
		return list;
	}
	
	
	
	
	
	
	
	
	//再次封装数据
	public MikuOperMeaureListData getOneMikuOperMeaureListData(int size,Long userId){
		MikuOperMeaureListData one=new MikuOperMeaureListData();
		Map<String,Object> map=getTypeParams(size,userId);
		List<MikuOperMeaureData> onelist=mikuInstrumentMeasureLogDOMapper.selectByDayParamsByAvg(map);
		for(int i=0;i<onelist.size();i++){
			MikuOperMeaureData nwadata=	onelist.get(i);
			nwadata.setTimetype((String) map.get("time"));
		}
		one.setList(onelist);
		one.setTime((String) map.get("time"));
		return one;
	}
	
	
	
	//获取各个月份的集合
	public List<MikuOperMeaureListData> getEveryMonth(List<String> strList,Long userId){
		List<MikuOperMeaureListData> list=new ArrayList<MikuOperMeaureListData>();
		for(int j=0;j<strList.size();j++){
			String str=strList.get(j);
			String[] arr=str.split("-");
			Long year=Long.parseLong(arr[0]);
			Long month=Long.parseLong(arr[1]);
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("year", year);
			map.put("id", userId);
			map.put("month",month);
			List<MikuOperMeaureData> onelist=mikuInstrumentMeasureLogDOMapper.selectByMonthParamsByAvg(map);
			for(int i=0;i<onelist.size();i++){
				MikuOperMeaureData nwadata=	onelist.get(i);
				nwadata.setTimetype(str);
			}
			MikuOperMeaureListData m=new MikuOperMeaureListData();
			m.setList(onelist);
			m.setTime(str);
			list.add(m);
		}
		return list;
	}
	
	
	
	
	
	
	//封装时间使用
	public Map<String,Object> getTypeParams(int size,Long userId){
		Date d=new Date();   
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd"); 
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("id", userId);
//		String str=df.format(new Date(d.getTime() - size * 24 * 60 * 60 * 1000));
		
	 	Calendar now = Calendar.getInstance();  
        now.setTime(d);  
        now.set(Calendar.DATE, now.get(Calendar.DATE) - size); 
        String str=df.format(now.getTime());
        
		map.put("time", str);
		String[] arr=str.split("-");
		map.put("year", Long.parseLong(arr[0]));
		map.put("month",Long.parseLong(arr[1]));
		map.put("day", Long.parseLong(arr[2]));
		return map;
	}
	
	
	//获取的当前的年月
	 public List<String> getNextMonth(int year,int month){
		   List<String> list=new ArrayList<String>();
		   List<String> nlist=new ArrayList<String>();
		   if(month==12){
			   for(int i=0;i<month;i++){
				   String str=(year+"-"+(month-i));
				   list.add(str);
			   }
		   }else{
			   int count=12-month;
			   for(int j=month;j>0;j--){
				   String str=(year+"-"+j);
				   list.add(str);
			   }
			   if(count>0){
				   for(int z=12;z>=(13-count);z--){
					   String str=((year-1)+"-"+z);
					   list.add(str);
				   } 
			   }
		   }
		   for(int i=list.size()-1;i>=0;i--){
			   nlist.add(list.get(i));
		   }
		   return nlist;
	   }
	 
	 
	 
	 //查找对应的记录
	 public MikuInstrumentMeasureLogDO getOneData(Long id){
		 	MikuInstrumentMeasureLogDOExample mikuInstrumentMeasureLogDOExample=new MikuInstrumentMeasureLogDOExample();
		 	mikuInstrumentMeasureLogDOExample.createCriteria().andIdEqualTo(id);
		 	List<MikuInstrumentMeasureLogDO> list=mikuInstrumentMeasureLogDOMapper.selectByExample(mikuInstrumentMeasureLogDOExample);
		 	MikuInstrumentMeasureLogDO measureLogDO=new MikuInstrumentMeasureLogDO();
		 	if(list.size()>0){
		 		measureLogDO=list.get(0);
		 	}
		 	return measureLogDO;
	 }
	
	 
	//专门分析分析对应的值[参数：测试值的类型]
	public List<MikuMineSkincareSuggestion> getAllListdata(){
	    	MikuMineSkincareSuggestionExample mikuMineSkincareSuggestionExample=new MikuMineSkincareSuggestionExample();
	    	mikuMineSkincareSuggestionExample.createCriteria().andIsDeleteEqualTo((byte) 0);
	    	List<MikuMineSkincareSuggestion> list=mikuMineSkincareSuggestionMapper.selectByExample(mikuMineSkincareSuggestionExample);
	    	return list;
	}
	
	
	//进行获取对应的商品列表
	public List<Item> getItemList(){
		ItemExample itemExample=new ItemExample();
		itemExample.createCriteria().andApproveStatusEqualTo((byte)1).andIdGreaterThan(13850L).andApproveStatusEqualTo((byte)1);
		List<Item> list=itemMapper.selectByExample(itemExample);
		ItemViewDO nitem=new ItemViewDO();
		
		return list;
	}
	
	
	
	
	
	
	
	//======================================2016.05.09进行对日期更改的排序=================================
	//修改对应的bug[改变查询风格]
	public List<MikuOperMeaureData> getNewDataListByParams(Long userId,String timeType){
		List<MikuInstrumentMeasureLogDO> AllList=new ArrayList<MikuInstrumentMeasureLogDO>();
		Map<String,Object> map=new HashMap<String,Object>();
		switch (timeType) {
		case "Y":
			Calendar cal = Calendar.getInstance();
	        int year = cal.get(Calendar.YEAR);//获取年份
	        int month=cal.get(Calendar.MONTH)+1;//获取月份
	        List<String> strList= getNextMonth(year,month);
	        AllList=getNewEveryMonth(strList,userId);
			break;
		case "M":
			for(int i=29;i>=0;i--){
				AllList.addAll(getOneNewMikuOperMeaureListData(i,userId));
			}		
			break;
		case "W":
			for(int i=6;i>=0;i--){
				AllList.addAll(getOneNewMikuOperMeaureListData(i,userId));
			}
			break;
		case "D":
			AllList.addAll(getOneNewMikuOperMeaureListData(0,userId));
			break;
		case "H":
			break;
		default:
			break;
		}
		List<MikuOperMeaureData> list= getNewAvgData(AllList);
		return list;
	}
	
	
	
//	  根据每天参数来获取总的数据列表
//    List<MikuInstrumentMeasureLogDO>  selectByOneDayForList(Map<String,Object> map);
//    
//    //根据每月参数来获取总的数据列表
//    List<MikuInstrumentMeasureLogDO>  selectByOneMonthForList(Map<String,Object> map);
	
	
	
	//再次封装数据
	public List<MikuInstrumentMeasureLogDO> getOneNewMikuOperMeaureListData(int size,Long userId){
			MikuOperMeaureListData one=new MikuOperMeaureListData();
			Map<String,Object> map=getTypeParams(size,userId);
			List<MikuInstrumentMeasureLogDO> onelist=mikuInstrumentMeasureLogDOMapper.selectByOneDayForList(map);
			return onelist;
	}
	
	
	//获取各个月份的集合
	public List<MikuInstrumentMeasureLogDO> getNewEveryMonth(List<String> strList,Long userId){
			List<MikuInstrumentMeasureLogDO> AllList=new ArrayList<MikuInstrumentMeasureLogDO>();
			for(int j=0;j<strList.size();j++){
				String str=strList.get(j);
				String[] arr=str.split("-");
				Long year=Long.parseLong(arr[0]);
				Long month=Long.parseLong(arr[1]);
				Map<String,Object> map=new HashMap<String,Object>();
				map.put("year", year);
				map.put("id", userId);
				map.put("month",month);
				List<MikuInstrumentMeasureLogDO> onelist=mikuInstrumentMeasureLogDOMapper.selectByOneMonthForList(map);
				AllList.addAll(onelist);
			}
			return AllList;
	}
	
	//进行总体的分类
	public List<MikuOperMeaureData> getNewAvgData(List<MikuInstrumentMeasureLogDO> list){
		List<MikuOperMeaureData> nlist=new ArrayList<MikuOperMeaureData>();
		for(int k=1;k<5;k++){
			MikuOperMeaureData m=new MikuOperMeaureData();
			BigDecimal measureV=new BigDecimal(0);
			BigDecimal oilV=new BigDecimal(0);
			BigDecimal resilienceV=new BigDecimal(0);
			BigDecimal senilityV=new BigDecimal(0);
			BigDecimal moistureV=new BigDecimal(0);
			int size=0;
			for(int i=0;i<list.size();i++){
				MikuInstrumentMeasureLogDO one=list.get(i);
				if((byte)k == one.getTestPosition()){
					size++;
					measureV=measureV.add(one.getMeasureValue());
					oilV=oilV.add(one.getOilValue());
					resilienceV=resilienceV.add(one.getResilienceValue());
					senilityV=senilityV.add(one.getSenilityValue());
					moistureV=moistureV.add(one.getMoistureValue());
				}
			}
			m.setTestPosition((byte)k);
			if(size>0){
				BigDecimal newmeasureV=measureV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newOilV=oilV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newResilienceV=resilienceV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newSenilityV=senilityV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newMoistureV=moistureV.divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_EVEN);
				m.setMeasureValue(newmeasureV);
				m.setOilValue(newOilV);
				m.setResilienceValue(newResilienceV);
				m.setSenilityValue(newSenilityV);
				m.setMoistureValue(newMoistureV);
			}else{
				m.setMeasureValue(measureV);
				m.setOilValue(oilV);
				m.setResilienceValue(resilienceV);
				m.setSenilityValue(senilityV);
				m.setMoistureValue(moistureV);
			}
			
			nlist.add(m);
		}
		return nlist;
	}

}
