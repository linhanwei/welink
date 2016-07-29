package com.welink.biz.profit;

/**
 * Created by myron on 16/3/6.
 */
public class ProfitContext {
    private ProfitState state;

    public void setState(ProfitState state) {
        this.state = state;
    }

    public void request(Long tradeId) {
        state.handle(tradeId);
    }
}
