package com.welink.commons.persistence;

import com.welink.commons.domain.MikuOneBuyDO;
import com.welink.commons.domain.MikuOneBuyDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface MikuOneBuyDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int countByExample(MikuOneBuyDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int deleteByExample(MikuOneBuyDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int insert(MikuOneBuyDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int insertSelective(MikuOneBuyDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    List<MikuOneBuyDO> selectByExampleWithRowbounds(MikuOneBuyDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    List<MikuOneBuyDO> selectByExample(MikuOneBuyDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    MikuOneBuyDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int updateByExampleSelective(@Param("record") MikuOneBuyDO record, @Param("example") MikuOneBuyDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int updateByExample(@Param("record") MikuOneBuyDO record, @Param("example") MikuOneBuyDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int updateByPrimaryKeySelective(MikuOneBuyDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_one_buy
     *
     * @mbggenerated Tue Dec 29 14:33:44 CST 2015
     */
    int updateByPrimaryKey(MikuOneBuyDO record);
}