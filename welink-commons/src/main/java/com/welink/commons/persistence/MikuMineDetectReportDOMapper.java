package com.welink.commons.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import com.welink.commons.domain.MikuMineDetectReportDO;
import com.welink.commons.domain.MikuMineDetectReportDOExample;
import com.welink.commons.vo.DetectReportTradeVO;

@Repository
public interface MikuMineDetectReportDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int countByExample(MikuMineDetectReportDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int deleteByExample(MikuMineDetectReportDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int insert(MikuMineDetectReportDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int insertSelective(MikuMineDetectReportDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    List<MikuMineDetectReportDO> selectByExampleWithRowbounds(MikuMineDetectReportDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    List<MikuMineDetectReportDO> selectByExample(MikuMineDetectReportDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    MikuMineDetectReportDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuMineDetectReportDO record, @Param("example") MikuMineDetectReportDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int updateByExample(@Param("record") MikuMineDetectReportDO record, @Param("example") MikuMineDetectReportDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int updateByPrimaryKeySelective(MikuMineDetectReportDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_detect_report
     *
     * @mbggenerated Fri May 13 11:11:41 CST 2016
     */
    int updateByPrimaryKey(MikuMineDetectReportDO record);
    
    /**
     * 获取报告订单
     * @param paramMap
     * @return
     */
    List<DetectReportTradeVO> getDetectReportTrades(Map<String, Object> paramMap);
}