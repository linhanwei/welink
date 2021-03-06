package com.welink.commons.persistence;

import com.welink.commons.domain.MikuMineCourseDO;
import com.welink.commons.domain.MikuMineCourseDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface MikuMineCourseDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int countByExample(MikuMineCourseDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int deleteByExample(MikuMineCourseDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int insert(MikuMineCourseDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int insertSelective(MikuMineCourseDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    List<MikuMineCourseDO> selectByExampleWithRowbounds(MikuMineCourseDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    List<MikuMineCourseDO> selectByExample(MikuMineCourseDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    MikuMineCourseDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuMineCourseDO record, @Param("example") MikuMineCourseDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByExample(@Param("record") MikuMineCourseDO record, @Param("example") MikuMineCourseDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByPrimaryKeySelective(MikuMineCourseDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_course
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByPrimaryKey(MikuMineCourseDO record);
}