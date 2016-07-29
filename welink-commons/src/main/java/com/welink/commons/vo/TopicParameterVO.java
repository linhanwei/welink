package com.welink.commons.vo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSON;

public class TopicParameterVO {
	Long min;
	Long max;
	Long value;
	public Long getMin() {
		return min;
	}
	public void setMin(Long min) {
		this.min = min;
	}
	public Long getMax() {
		return max;
	}
	public void setMax(Long max) {
		this.max = max;
	}
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}
	
	public static void main(String[] args) {
		List<TopicParameterVO> topicParameterVOList = JSON.parseArray("[{\"min\":300,\"value\":30},{\"min\":200,\"value\":20},{\"min\":100,\"value\":10},{\"min\":400,\"value\":40}]", TopicParameterVO.class);
		if(null != topicParameterVOList && !topicParameterVOList.isEmpty()){
			Collections.sort(topicParameterVOList, new Comparator<TopicParameterVO>() {
                public int compare(TopicParameterVO arg0, TopicParameterVO arg1) {
                    //return arg0.getMin().compareTo(arg1.getMin());
                	int i = 0;
                	i = arg0.getMin().compareTo(arg1.getMin());
                	if(i > 0){
                		return -1;
                	}else{
                		return 1;
                	}
                }
            });
			for(TopicParameterVO topicParameterVO : topicParameterVOList){
				System.out.println(topicParameterVO.getMin()+"-----Value-"+topicParameterVO.getValue());
			}
		}
	}
	
}
