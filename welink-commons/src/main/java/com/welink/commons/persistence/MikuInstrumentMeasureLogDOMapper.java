package com.welink.commons.persistence;

import com.welink.commons.domain.MikuInstrumentMeasureLogDO;
import com.welink.commons.domain.MikuInstrumentMeasureLogDOExample;
import com.welink.commons.domain.MikuOperMeaureData;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;
@Repository
public interface MikuInstrumentMeasureLogDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int countByExample(MikuInstrumentMeasureLogDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int deleteByExample(MikuInstrumentMeasureLogDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int insert(MikuInstrumentMeasureLogDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int insertSelective(MikuInstrumentMeasureLogDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    List<MikuInstrumentMeasureLogDO> selectByExampleWithRowbounds(MikuInstrumentMeasureLogDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    List<MikuInstrumentMeasureLogDO> selectByExample(MikuInstrumentMeasureLogDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    MikuInstrumentMeasureLogDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuInstrumentMeasureLogDO record, @Param("example") MikuInstrumentMeasureLogDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int updateByExample(@Param("record") MikuInstrumentMeasureLogDO record, @Param("example") MikuInstrumentMeasureLogDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int updateByPrimaryKeySelective(MikuInstrumentMeasureLogDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_instrument_measure_log
     *
     * @mbggenerated Tue Apr 19 16:45:46 CST 2016
     */
    int updateByPrimaryKey(MikuInstrumentMeasureLogDO record);
    
    
    //计算每个月平均值
    List<MikuOperMeaureData> selectByMonthParamsByAvg(Map<String,Object> map);
    //这是每天的平均值
    List<MikuOperMeaureData> selectByDayParamsByAvg(Map<String,Object> map);
    //这是每个小时
    List<MikuOperMeaureData> selectByHourParamsByAvg(Map<String,Object> map);
    
    
    //根据每天参数来获取总的数据列表
    List<MikuInstrumentMeasureLogDO>  selectByOneDayForList(Map<String,Object> map);
    
    //根据每月参数来获取总的数据列表
    List<MikuInstrumentMeasureLogDO>  selectByOneMonthForList(Map<String,Object> map);
    
}