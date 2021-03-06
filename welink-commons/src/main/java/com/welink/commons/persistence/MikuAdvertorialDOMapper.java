package com.welink.commons.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import com.welink.commons.domain.MikuAdvertorialDO;
import com.welink.commons.domain.MikuAdvertorialDOExample;

@Repository
public interface MikuAdvertorialDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int countByExample(MikuAdvertorialDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int deleteByExample(MikuAdvertorialDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int insert(MikuAdvertorialDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int insertSelective(MikuAdvertorialDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    List<MikuAdvertorialDO> selectByExampleWithRowbounds(MikuAdvertorialDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    List<MikuAdvertorialDO> selectByExample(MikuAdvertorialDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    MikuAdvertorialDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuAdvertorialDO record, @Param("example") MikuAdvertorialDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int updateByExample(@Param("record") MikuAdvertorialDO record, @Param("example") MikuAdvertorialDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int updateByPrimaryKeySelective(MikuAdvertorialDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_advertorial
     *
     * @mbggenerated Fri Apr 22 17:14:52 CST 2016
     */
    int updateByPrimaryKey(MikuAdvertorialDO record);
}