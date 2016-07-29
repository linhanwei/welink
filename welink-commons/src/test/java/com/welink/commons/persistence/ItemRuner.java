/**
 * Project Name:welink-commons
 * File Name:ItemRuner.java
 * Package Name:com.welink.commons.persistence
 * Date:2015年11月19日下午9:36:25
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.transaction.TransactionStatus;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.GrouponDO;
import com.welink.commons.domain.GrouponDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.Order;

/**
 * ClassName:ItemRuner <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月19日 下午9:36:25 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ItemRuner implements Runnable{
	
	private GrouponDOMapper grouponDOMapper;
    private ItemMapper itemMapper;
    private String name;
    
    
	public ItemRuner(GrouponDOMapper grouponDOMapper, ItemMapper itemMapper, String name) {
		super();
		this.grouponDOMapper = grouponDOMapper;
		this.itemMapper = itemMapper;
		this.name = name;
	}


	@Override
	public void run() {
		System.out.println("name:"+name);
		Item item = this.itemMapper.selectByPrimaryKey(7691L);
		System.out.println("1111111111111111111111111111111111111111111111111111111111111");
		System.out.println(item.getDescription()+".................num...."+item.getNum());
		/*for(int i=0; i<25; i++){
			System.out.print("..i:"+i);
			updateStock();
		}*/
		
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
            List<GrouponDO> grouponDOs = this.grouponDOMapper.selectByExample(gExample);
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
                if (this.grouponDOMapper.updateByExampleSelective(grouponDO, gExample1) < 1) {
                	//logger.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
                    return true;
                } else {
                    //更新商品销售数量
                    Item item = this.itemMapper.selectByPrimaryKey(itemId);
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
                        if (this.itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                        	//logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            //com.welink.web.common.filter.Profiler.release();
                            return true;
                        }
                    }
                }
            } else {//非团购商品  更新item表中的库存
                Item item = this.itemMapper.selectByPrimaryKey(itemId);
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
                    if (this.itemMapper.updateByExampleSelective(item1, iExample) < 1) {
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

