package com.welink.biz.profit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.jdbc.Connection;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * Created by myron on 16/3/6.
 */
@Service
public class CutProfitImpl implements InitializingBean {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(CutProfitImpl.class);
	
	@Resource
    private TradeMapper tradeMapper;
    
    @Resource
    private PlatformTransactionManager transactionManager;
	
	private TransactionTemplate transactionTemplate;
	
	@Resource
    private ProfileDOMapper profileDOMapper;
	
	@Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
	
	@Resource
	private OrderMapper orderMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	/**
	 * 
	 * handle:(减分润处理). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param orderId
	 */
    public void handle(final Long orderId) {
    	transactionTemplate
		.execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(
					TransactionStatus transactionStatus) {
				
				Order order = orderMapper.selectByPrimaryKey(orderId);
				if(null != order && null != order.getArtificialId() && order.getArtificialId() > 0 && !order.getIsReturnProfit().equals((byte)1)){
					//order.setReturnStatus(Constants.ReturnGoodsStatus.FINISHED.getStatusId());
					order.setIsReturnProfit((byte)1);	//是否退分润(0=没退;1=已退)
					if(orderMapper.updateByPrimaryKeySelective(order) < 1){
						transactionStatus.setRollbackOnly();
						return false;
					}
					MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
					mikuSalesRecordDOExample.createCriteria().andTradeIdEqualTo(order.getTradeId())
						.andItemIdEqualTo(order.getArtificialId());
					List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectByExample(mikuSalesRecordDOExample);
					boolean isAgency = false;	//购买者购买时是否为代理（true=是；flase=不是）
					Long totalShareFee = 0L;	//总分润
					Long shareFee = 0L;			//分润
					Long noGetpayFee = 0L;		//未提分润
					for(MikuSalesRecordDO mikuSalesRecordDO : mikuSalesRecordDOList){
						//更新代理帐户信息
						MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
						mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(mikuSalesRecordDO.getAgencyId());
						List<MikuAgencyShareAccountDO> agencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
						if(null != agencyShareAccountDOList && !agencyShareAccountDOList.isEmpty()){
							if(null != mikuSalesRecordDO && null != mikuSalesRecordDO.getId()){
								mikuSalesRecordDO.setReturnStatus(Constants.ReturnGoodsStatus.FINISHED.getStatusId());
								mikuSalesRecordDO.setReturnDate(new Date());
								//把分润记录表变成退货完成
								if(mikuSalesRecordDOMapper.updateByPrimaryKeySelective(mikuSalesRecordDO) < 1){
									transactionStatus.setRollbackOnly();
									return false;
								}
							}
							MikuAgencyShareAccountDO agencyShareAccountDO = agencyShareAccountDOList.get(0);  
							totalShareFee = (null == agencyShareAccountDO.getTotalShareFee() 
									? 0L : agencyShareAccountDO.getTotalShareFee());
							noGetpayFee = (null == agencyShareAccountDO.getNoGetpayFee() 
									? 0L : agencyShareAccountDO.getNoGetpayFee());
							shareFee = (null == mikuSalesRecordDO.getShareFee() ? 0L : mikuSalesRecordDO.getShareFee());
							if((totalShareFee - shareFee) >= 0){
								agencyShareAccountDO.setTotalShareFee(totalShareFee - shareFee);
								agencyShareAccountDO.setNoGetpayFee((noGetpayFee - shareFee)<0 ? 0L : (noGetpayFee - shareFee));
								agencyShareAccountDO.setLastUpdated(new Date());
								if(mikuAgencyShareAccountDOMapper.updateByPrimaryKey(agencyShareAccountDO) < 0){
									transactionStatus.setRollbackOnly();
									return false;
								}
							}
						}
					}
					
				}
				return true;
			}
		});
    }
    
    @Override
	public void afterPropertiesSet() throws Exception {
		
		checkNotNull(profileDOMapper);
		checkNotNull(transactionManager);
		checkNotNull(mikuSalesRecordDOMapper);
		checkNotNull(mikuAgencyShareAccountDOMapper);
		checkNotNull(tradeMapper);
		//checkNotNull(orderMapper);

        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("userAgentcy-transaction");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
		
	}
    
}
