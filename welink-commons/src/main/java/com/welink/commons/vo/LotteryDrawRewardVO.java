/**
 * Project Name:welink-commons
 * File Name:RewardVO.java
 * Package Name:com.welink.commons.vo
 * Date:2015年12月21日下午3:15:46
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.vo;
/**
 * ClassName:RewardVO <br/>
 * Function: 抽奖奖品 <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月21日 下午3:15:46 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class LotteryDrawRewardVO {
	private Long id;
	private Integer index;	//序号(100X=积分;200X=优惠券;300X=商品)
	private String name;
	private Integer type;	//(-1=谢谢；1=积分；2=优惠券；3=商品)
	private Long rewardId;	//奖品id,如商品id
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public Long getRewardId() {
		return rewardId;
	}
	public void setRewardId(Long rewardId) {
		this.rewardId = rewardId;
	}
	
}

