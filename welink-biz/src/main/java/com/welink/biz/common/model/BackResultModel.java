package com.welink.biz.common.model;

import com.alibaba.fastjson.JSON;

/**
 * Created by daniel on 14-12-31.
 */
public class BackResultModel {
    int status;
    String result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public static void main(String[] args) {
        String result = "{\"status\":1\n" +
                ",\n" +
                "\"result\":{\n" +
                "\"fm\":\"<form id=\\\"alipaysubmit\\\" name=\\\"alipaysubmit\\\" action=\\\"http:\\/\\/wappaygw.alipay.com\\/service\\/rest.htm?\\\" method=\\\"get\\\"><input type=\\\"hidden\\\" \n" +
                "name=\\\"v\\\" value=\\\"2.0\\\"\\/><input type=\\\"hidden\\\" name=\\\"sec_id\\\" value=\\\"0001\\\"\\/><input type=\\\"hidden\\\" name=\\\"_input_charset\\\" value=\\\"utf-8\\\"\\/><in\n" +
                "put type=\\\"hidden\\\" name=\\\"req_data\\\" value=\\\"<auth_and_execute_req><request_token>20141231a9f404dc79a9dc01e3b3d585079b0c85<\\/request_token><\\/auth_and\n" +
                "_execute_req>\\\"\\/><input type=\\\"hidden\\\" name=\\\"service\\\" value=\\\"alipay.wap.auth.authAndExecute\\\"\\/><input type=\\\"hidden\\\" name=\\\"partner\\\" value=\\\"20\n" +
                "88611242254399\\\"\\/><input type=\\\"hidden\\\" name=\\\"format\\\" value=\\\"xml\\\"\\/><input type=\\\"hidden\\\" name=\\\"sign\\\" value=\\\"SoxEW9lCwKu\\/X8x55nhtKPsGX0it\\/v\n" +
                "qbyxCbMHxsNRi6V\\/DjkOHazfwdvDeN9712lG0wssyKI+9aCrPshjSsLVDaasjDZ+yw7jVBCBLXLTJRiQvfJo+EjSyMQfgYpF3U5UZH6TJOwTVrd2aQaIExpizH\\/a17OiYna\\/BYG+QABdE=\\\"\\/><\n" +
                "input type=\\\"submit\\\" value=\\\"\\u786E\\u8BA4\\\" style=\\\"display:none;\\\"><\\/form><script>document.forms['alipaysubmit'].submit();<\\/script>\"\n" +
                "}\n" +
                "}\n";
        BackResultModel back = JSON.parseObject(result, BackResultModel.class);

        System.out.println(back.getResult());
    }
}
