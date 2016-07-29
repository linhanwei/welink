package com.welink.commons.persistence;

import com.welink.commons.domain.MikuMineQuestionsDO;
import com.welink.commons.domain.MikuMineQuestionsDOExample;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;
@Repository
public interface MikuMineQuestionsDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int countByExample(MikuMineQuestionsDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int deleteByExample(MikuMineQuestionsDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int insert(MikuMineQuestionsDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int insertSelective(MikuMineQuestionsDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    List<MikuMineQuestionsDO> selectByExampleWithRowbounds(MikuMineQuestionsDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    List<MikuMineQuestionsDO> selectByExample(MikuMineQuestionsDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    MikuMineQuestionsDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuMineQuestionsDO record, @Param("example") MikuMineQuestionsDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int updateByExample(@Param("record") MikuMineQuestionsDO record, @Param("example") MikuMineQuestionsDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int updateByPrimaryKeySelective(MikuMineQuestionsDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_questions
     *
     * @mbggenerated Thu May 19 17:52:21 CST 2016
     */
    int updateByPrimaryKey(MikuMineQuestionsDO record);
    
    
    List<MikuMineQuestionsDO> selectQuestionByids(Map<String,Object> map);
}