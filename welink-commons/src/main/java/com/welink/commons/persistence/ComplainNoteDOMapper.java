package com.welink.commons.persistence;

import com.welink.commons.domain.ComplainNoteDO;
import com.welink.commons.domain.ComplainNoteDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplainNoteDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int countByExample(ComplainNoteDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int deleteByExample(ComplainNoteDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int insert(ComplainNoteDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int insertSelective(ComplainNoteDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    List<ComplainNoteDO> selectByExampleWithRowbounds(ComplainNoteDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    List<ComplainNoteDO> selectByExample(ComplainNoteDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    ComplainNoteDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByExampleSelective(@Param("record") ComplainNoteDO record, @Param("example") ComplainNoteDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByExample(@Param("record") ComplainNoteDO record, @Param("example") ComplainNoteDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByPrimaryKeySelective(ComplainNoteDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table complain_deal_notes
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByPrimaryKey(ComplainNoteDO record);
}