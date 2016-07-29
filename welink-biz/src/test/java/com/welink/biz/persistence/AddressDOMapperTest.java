package com.welink.biz.persistence;

import com.welink.commons.domain.AddressDO;
import com.welink.commons.domain.AddressDOExample;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.CommunityDOExample;
import com.welink.biz.service.AddressService;
import com.welink.commons.persistence.AddressDOMapper;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-biz.xml"})
@ActiveProfiles("dev")
public class AddressDOMapperTest {

    Logger logger = LoggerFactory.getLogger(AddressDOMapperTest.class);

    @Resource
    private AddressDOMapper addressDOMapper;

    @Resource
    private AddressService addressService;

    @Resource
    private CommunityDOMapper communityDOMapper;

    @Test
    public void testAddressService() {
        CommunityDOExample communityDOExample = new CommunityDOExample();
        communityDOExample.createCriteria().andCityEqualTo("杭州");
        List<CommunityDO> communityDOs = communityDOMapper.selectByExample(communityDOExample);
        System.out.println("===========0===========");
        for (CommunityDO c : communityDOs) {
            System.out.println(c.getId());
            System.out.println(c.getName());
        }

        List<AddressDO> addressDOs = addressService.fetchAddressByCommunityId_lv1(199l, (byte) -1, 0);
        System.out.println("============1============");
        for (AddressDO add : addressDOs) {
            System.out.println(add.getContent());
            System.out.println("id:" + add.getId());
            System.out.println("type:" + add.getType());
        }
        System.out.println("========================");

        List<AddressDO> addressDOs2 = addressService.fetchAddressByCommunityId_lv1(199l, (byte) -1, 5);
        System.out.println("=============2start===========");
        for (AddressDO add : addressDOs2) {
            System.out.println(add.getContent());
            System.out.println("id2:" + add.getId());
            System.out.println("type:" + add.getType());
        }
        System.out.println("============2end============");

        List<AddressDO> addressDOs3 = addressService.fetchAddressByCommunityId_lv1(199l, (byte) -1, 7);
        System.out.println("==============3s==========");
        for (AddressDO add : addressDOs3) {
            System.out.println(add.getContent());
            System.out.println("id:" + add.getId());
        }
        System.out.println("============3e============");

        List<AddressDO> addressDOs4 = addressService.fetchAddressByCommunityId_lv1(199l, (byte) 4, 2);
        System.out.println("========================");
        for (AddressDO add : addressDOs4) {
            System.out.println(add.getContent());
            System.out.println("id:" + add.getId());
        }
        System.out.println("========================");
    }

    @Test
    public void demo() {
        AddressDOExample level1 = new AddressDOExample();
        level1.createCriteria() //
                .andCommunityIdEqualTo(199L) //
                .andLevelEqualTo((byte) 1);

        // 第一级开始
        List<AddressDO> addressDOs = addressDOMapper.selectByExample(level1);
        MatcherAssert.assertThat(addressDOs.size(), greaterThan(1));
        MatcherAssert.assertThat(addressDOs.get(0).getType(), equalTo((byte) 1));
        println(addressDOs);

        // 第一级结束

        // 第二级开始
        AddressDOExample level2 = new AddressDOExample();
        level2.createCriteria().andCommunityIdEqualTo(199L).andParentIdEqualTo(390L);
        addressDOs = addressDOMapper.selectByExample(level2);
        MatcherAssert.assertThat(addressDOs.size(), greaterThan(1));
        MatcherAssert.assertThat(addressDOs.get(0).getType(), nullValue());
        println(addressDOs);
        // 第二级结束

        // 第三级开始
        AddressDOExample level3 = new AddressDOExample();
        level3.createCriteria().andCommunityIdEqualTo(199L).andParentIdEqualTo(394L);
        addressDOs = addressDOMapper.selectByExample(level3);
        MatcherAssert.assertThat(addressDOs.size(), greaterThan(1));
        MatcherAssert.assertThat(addressDOs.get(0).getType(), nullValue());
        println(addressDOs);
        // 第三级结束

        // 第四级开始
        AddressDOExample level4 = new AddressDOExample();
        level4.createCriteria().andCommunityIdEqualTo(199L).andParentIdEqualTo(558L);
        addressDOs = addressDOMapper.selectByExample(level4);
        println(addressDOs);
        MatcherAssert.assertThat(addressDOs.size(), greaterThan(1));
        MatcherAssert.assertThat(addressDOs.get(0).getType(), equalTo((byte) 2));
        MatcherAssert.assertThat(addressDOs.get(0).getBuildingId(), notNullValue());
        // 第四级结束
    }

    synchronized void println(List<AddressDO> addressDOs) {
        logger.info("------------------------------");
        for (AddressDO addressDO : addressDOs)
            logger.info(ToStringBuilder.reflectionToString(addressDO, new NoNullFieldStringStyle()));
        logger.info("------------------------------");
    }

}