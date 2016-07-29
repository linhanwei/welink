package com.welink.biz.common.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by daniel on 15-1-29.
 */
public class SearchResult<T> {

    private boolean success;

    private Error error;

    private Result<T> result;

    public boolean isSuccess() {
        return success;
    }

    public Error getError() {
        return error;
    }

    public Result<T> getResult() {
        return result;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public void setResult(Result<T> result) {
        this.result = result;
    }

    public static class Error {
        private String code;

        private String message;

        public Error(String message, String code) {
            this.message = message;
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class Result<T> {
        //查询耗时
        private float searchTime;
        //被索引收录的相关条目
        private int total;
        //当前页返回数
        private int num;
        //引擎实际返回的总数
        private int viewTotal;
        //返回的object
        private List<T> resultList = Lists.newArrayList();

        public float getSearchTime() {
            return searchTime;
        }

        public void setSearchTime(float searchTime) {
            this.searchTime = searchTime;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getViewTotal() {
            return viewTotal;
        }

        public void setViewTotal(int viewTotal) {
            this.viewTotal = viewTotal;
        }

        public List<T> getResultList() {
            return resultList;
        }

        public void setResultList(List<T> resultList) {
            this.resultList = resultList;
        }
    }


    public static <T> SearchResult<T> failure(String code, String message) {
        SearchResult<T> searchResult = new SearchResult<T>();
        searchResult.setSuccess(false);
        searchResult.setError(new Error(code, message));
        return searchResult;
    }

}
