/**
 * Project Name:welink-commons
 * File Name:ItemMapperTest.java
 * Package Name:com.welink.commons.persistence
 * Date:2015年11月19日下午9:01:03
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.GrouponDO;
import com.welink.commons.domain.GrouponDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.Order;
import com.welink.commons.vo.ItemTagActivtyVO;

/**
 * ClassName:ItemMapperTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月19日 下午9:01:03 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
//@RunWith(Parameterized.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("dev")
public class ItemMapperTest {
	@Resource
	public GrouponDOMapper grouponDOMapper;
	
	@Resource
    private ItemMapper itemMapper;
	
	@Resource
    private TradeMapper tradeMapper;
	
	private String name;
	
	/*gc.add(1,-1)表示年份减一.
	*gc.add(2,-1)表示月份减一.
	*gc.add(3.-1)表示周减一.
	*gc.add(5,-1)表示天减一.*/
	public Date addDate(Date date, int type, int day){
		GregorianCalendar gc=new GregorianCalendar(); 
		gc.setTime(date); 
		gc.add(5,30); 
		date = gc.getTime();
		return date;
	}
	
	@Test
	public void selectItemTagByParams(){
		System.out.println("----------------------------");
		String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";  
		SimpleDateFormat df = null;  
        Date date = null;  
        df = new SimpleDateFormat(DATETIME_FORMAT);  
  
        try {  
            date = df.parse("2015-11-11 16:55:33");  
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("bit", 4);
            map.put("startTime", date);
            //map.put("endTime", date.);
            //map.put("endTime", addDate(date, 5, 30));
            System.out.println("---................."+df.format(new Date(date.getTime())));
            System.out.println("................."+df.format(addDate(date, 5, 30)));
            List<ItemTagActivtyVO> items = itemMapper.selectItemTagByParams(map);
            for(ItemTagActivtyVO item : items){
            	System.out.println(".................title:"+item.getTitle()+".....TagName:"+item.getTagName());
            }
        } catch (ParseException pe) {  
        }  
	}

	@Test
	public void test(){
		System.out.println("----------------------------");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("num", -1);
		//map.put("itemId", 7786L);
		map.put("objectTaggedId", 4277L);
		//tradeMapper.updateItemNum(map);
		//tradeMapper.updateGrouponItemNum(map);
		if(tradeMapper.updateObjectTaggedNumById(map) > 0){
			System.out.println("1111111111111111111111111111111111111111");
		}else{
			System.out.println("2222222222222222222222222222222222222222222");
		}
		/*ItemRuner item = new ItemRuner(grouponDOMapper, itemMapper, "lgc");
		Thread tr = new Thread(item);
		tr.start();
		Item item = itemMapper.selectByPrimaryKey(7691L);
		System.out.println("1111111111111111111111111111111111111111111111111111111111111");
		System.out.println(item.getDescription()+".................num...."+item.getNum());*/
	}
	
	@Test 
    public void MultiRequestsTest() { 
		System.out.println("1111111111111111111111111111111111111");
                // 构造一个Runner 
        TestRunnable runner = new TestRunnable() { 
            @Override 
            public void runTest() throws Throwable { 
                // 测试内容 
            	updateStock2();
            } 
        }; 
        int runnerCount = 100; 
                //Rnner数组，想当于并发多少个。 
        TestRunnable[] trs = new TestRunnable[runnerCount]; 
        for (int i = 0; i < runnerCount; i++) { 
            trs[i] = runner; 
        } 
                // 用于执行多线程测试用例的Runner，将前面定义的单个Runner组成的数组传入 
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs); 
        try { 
                        // 开发并发执行数组里定义的内容 
            mttr.runTestRunnables(); 
        } catch (Throwable e) { 
            e.printStackTrace(); 
        } 
    } 
	
	private void updateStock2() {
		long itemId = 7786;
		int num = 1;
        GrouponDOExample gExample = new GrouponDOExample();
        gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
        gExample.setOrderByClause("online_end_time DESC");
        List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
        //是团购商品
        if (null != grouponDOs && grouponDOs.size() > 0) {
        	Map<String,Object> map = new HashMap<String, Object>();
    		map.put("num", num);
    		map.put("itemId", 7786L);
    		if(tradeMapper.updateGrouponItemNum(map) < 1){
    			return;
    		}else{
    			tradeMapper.updateItemNum(map);
    		}
        }else{
        	Map<String,Object> map = new HashMap<String, Object>();
    		map.put("num", num);
    		map.put("itemId", 7786L);
    		tradeMapper.updateItemNum(map);
    		//tradeMapper.updateGrouponItemNum(map);
        }
	}
	
	
	/**
     * 更新库存
     *
     * @param order
     * @return
     */
    //private boolean updateStock(Order order, TransactionStatus transactionStatus) {
	private boolean updateStock() {
        //if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
            /*long itemId = order.getArtificialId();
            long num = order.getNum();*/
			long itemId = 7691;
			long num = 1;
            GrouponDOExample gExample = new GrouponDOExample();
            gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
            gExample.setOrderByClause("online_end_time DESC");
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
            //是团购商品
            if (null != grouponDOs && grouponDOs.size() > 0) {
                long currentStock = grouponDOs.get(0).getQuantity();
                long soldQuantity = grouponDOs.get(0).getSoldQuantity();
                long grouponId = grouponDOs.get(0).getId();
                long version = grouponDOs.get(0).getVersion();
                currentStock = currentStock - num;
                if (currentStock < 0) {
                    currentStock = 0;
                }
                soldQuantity = soldQuantity + num;
                GrouponDO grouponDO = new GrouponDO();
                grouponDO.setQuantity(currentStock);
                grouponDO.setSoldQuantity(soldQuantity);
                grouponDO.setVersion(version + 1l);
                GrouponDOExample gExample1 = new GrouponDOExample();
                gExample1.createCriteria().andIdEqualTo(grouponId).andVersionEqualTo(version);
                if (grouponDOMapper.updateByExampleSelective(grouponDO, gExample1) < 1) {
                	//logger.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
                    return true;
                } else {
                    //更新商品销售数量
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    if (null != item) {
                        long itemSoldQuantity = 0;
                        if (null == item.getSoldQuantity()) {
                            itemSoldQuantity = 0;
                        } else {
                            if (null == item.getSoldQuantity()) {
                                itemSoldQuantity = 0;
                            } else {
                                itemSoldQuantity = item.getSoldQuantity();
                            }
                        }
                        itemSoldQuantity = itemSoldQuantity + num;
                        Item item1 = new Item();
                        item1.setSoldQuantity((int) itemSoldQuantity);
                        long itemNum = item.getNum();
                        itemNum = itemNum - num;
                        item1.setNum((int) itemNum);
                        item1.setVersion(item.getVersion() + 1l);
                        item1.setLastUpdated(new Date());
                        ItemExample iExample = new ItemExample();
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                        if (itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                        	//logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            //com.welink.web.common.filter.Profiler.release();
                            return true;
                        }
                    }
                }
            } else {//非团购商品  更新item表中的库存
                Item item = itemMapper.selectByPrimaryKey(itemId);
                if (null != item) {
                    long itemSoldQuantity = 0;
                    if (null == item.getSoldQuantity()) {
                        itemSoldQuantity = 0;
                    } else {
                        if (null == item.getSoldQuantity()) {
                            itemSoldQuantity = 0;
                        } else {
                            itemSoldQuantity = item.getSoldQuantity();
                        }
                    }
                    itemSoldQuantity = itemSoldQuantity + num;
                    Item item1 = new Item();
                    item1.setSoldQuantity((int) itemSoldQuantity);
                    long itemNum = item.getNum();
                    itemNum = itemNum - num;
                    ItemExample iExample = new ItemExample();
                    item1.setVersion(item.getVersion() + 1l);
                    item1.setLastUpdated(new Date());
                    if (itemNum <= 0) {
                        //下架商品
                        item1.setNum(0);
                        item1.setApproveStatus(BizConstants.ItemApproveStatus.OFF_SALE.getStatus());
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    } else {
                        item1.setNum((int) itemNum);
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    }
                    if (itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                    	//logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                        ///com.welink.web.common.filter.Profiler.release();
                        return true;
                    }
                }
            }
        //}
        return false;
    }
	
}

