package com.welink.commons.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AvosNexusDOExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected List<Criteria> oredCriteria;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected Integer offset;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected Integer limit;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public AvosNexusDOExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andProfileIdIsNull() {
            addCriterion("profile_id is null");
            return (Criteria) this;
        }

        public Criteria andProfileIdIsNotNull() {
            addCriterion("profile_id is not null");
            return (Criteria) this;
        }

        public Criteria andProfileIdEqualTo(Long value) {
            addCriterion("profile_id =", value, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdNotEqualTo(Long value) {
            addCriterion("profile_id <>", value, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdGreaterThan(Long value) {
            addCriterion("profile_id >", value, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("profile_id >=", value, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdLessThan(Long value) {
            addCriterion("profile_id <", value, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdLessThanOrEqualTo(Long value) {
            addCriterion("profile_id <=", value, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdIn(List<Long> values) {
            addCriterion("profile_id in", values, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdNotIn(List<Long> values) {
            addCriterion("profile_id not in", values, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdBetween(Long value1, Long value2) {
            addCriterion("profile_id between", value1, value2, "profileId");
            return (Criteria) this;
        }

        public Criteria andProfileIdNotBetween(Long value1, Long value2) {
            addCriterion("profile_id not between", value1, value2, "profileId");
            return (Criteria) this;
        }

        public Criteria andInstallationIsNull() {
            addCriterion("installation is null");
            return (Criteria) this;
        }

        public Criteria andInstallationIsNotNull() {
            addCriterion("installation is not null");
            return (Criteria) this;
        }

        public Criteria andInstallationEqualTo(String value) {
            addCriterion("installation =", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationNotEqualTo(String value) {
            addCriterion("installation <>", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationGreaterThan(String value) {
            addCriterion("installation >", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationGreaterThanOrEqualTo(String value) {
            addCriterion("installation >=", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationLessThan(String value) {
            addCriterion("installation <", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationLessThanOrEqualTo(String value) {
            addCriterion("installation <=", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationLike(String value) {
            addCriterion("installation like", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationNotLike(String value) {
            addCriterion("installation not like", value, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationIn(List<String> values) {
            addCriterion("installation in", values, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationNotIn(List<String> values) {
            addCriterion("installation not in", values, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationBetween(String value1, String value2) {
            addCriterion("installation between", value1, value2, "installation");
            return (Criteria) this;
        }

        public Criteria andInstallationNotBetween(String value1, String value2) {
            addCriterion("installation not between", value1, value2, "installation");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenIsNull() {
            addCriterion("device_token is null");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenIsNotNull() {
            addCriterion("device_token is not null");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenEqualTo(String value) {
            addCriterion("device_token =", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenNotEqualTo(String value) {
            addCriterion("device_token <>", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenGreaterThan(String value) {
            addCriterion("device_token >", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenGreaterThanOrEqualTo(String value) {
            addCriterion("device_token >=", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenLessThan(String value) {
            addCriterion("device_token <", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenLessThanOrEqualTo(String value) {
            addCriterion("device_token <=", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenLike(String value) {
            addCriterion("device_token like", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenNotLike(String value) {
            addCriterion("device_token not like", value, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenIn(List<String> values) {
            addCriterion("device_token in", values, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenNotIn(List<String> values) {
            addCriterion("device_token not in", values, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenBetween(String value1, String value2) {
            addCriterion("device_token between", value1, value2, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDeviceTokenNotBetween(String value1, String value2) {
            addCriterion("device_token not between", value1, value2, "deviceToken");
            return (Criteria) this;
        }

        public Criteria andDateCreatedIsNull() {
            addCriterion("date_created is null");
            return (Criteria) this;
        }

        public Criteria andDateCreatedIsNotNull() {
            addCriterion("date_created is not null");
            return (Criteria) this;
        }

        public Criteria andDateCreatedEqualTo(Date value) {
            addCriterion("date_created =", value, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedNotEqualTo(Date value) {
            addCriterion("date_created <>", value, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedGreaterThan(Date value) {
            addCriterion("date_created >", value, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedGreaterThanOrEqualTo(Date value) {
            addCriterion("date_created >=", value, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedLessThan(Date value) {
            addCriterion("date_created <", value, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedLessThanOrEqualTo(Date value) {
            addCriterion("date_created <=", value, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedIn(List<Date> values) {
            addCriterion("date_created in", values, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedNotIn(List<Date> values) {
            addCriterion("date_created not in", values, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedBetween(Date value1, Date value2) {
            addCriterion("date_created between", value1, value2, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andDateCreatedNotBetween(Date value1, Date value2) {
            addCriterion("date_created not between", value1, value2, "dateCreated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedIsNull() {
            addCriterion("last_updated is null");
            return (Criteria) this;
        }

        public Criteria andLast_updatedIsNotNull() {
            addCriterion("last_updated is not null");
            return (Criteria) this;
        }

        public Criteria andLast_updatedEqualTo(Date value) {
            addCriterion("last_updated =", value, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedNotEqualTo(Date value) {
            addCriterion("last_updated <>", value, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedGreaterThan(Date value) {
            addCriterion("last_updated >", value, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedGreaterThanOrEqualTo(Date value) {
            addCriterion("last_updated >=", value, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedLessThan(Date value) {
            addCriterion("last_updated <", value, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedLessThanOrEqualTo(Date value) {
            addCriterion("last_updated <=", value, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedIn(List<Date> values) {
            addCriterion("last_updated in", values, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedNotIn(List<Date> values) {
            addCriterion("last_updated not in", values, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedBetween(Date value1, Date value2) {
            addCriterion("last_updated between", value1, value2, "last_updated");
            return (Criteria) this;
        }

        public Criteria andLast_updatedNotBetween(Date value1, Date value2) {
            addCriterion("last_updated not between", value1, value2, "last_updated");
            return (Criteria) this;
        }

        public Criteria andChannelsIsNull() {
            addCriterion("channels is null");
            return (Criteria) this;
        }

        public Criteria andChannelsIsNotNull() {
            addCriterion("channels is not null");
            return (Criteria) this;
        }

        public Criteria andChannelsEqualTo(String value) {
            addCriterion("channels =", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsNotEqualTo(String value) {
            addCriterion("channels <>", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsGreaterThan(String value) {
            addCriterion("channels >", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsGreaterThanOrEqualTo(String value) {
            addCriterion("channels >=", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsLessThan(String value) {
            addCriterion("channels <", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsLessThanOrEqualTo(String value) {
            addCriterion("channels <=", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsLike(String value) {
            addCriterion("channels like", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsNotLike(String value) {
            addCriterion("channels not like", value, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsIn(List<String> values) {
            addCriterion("channels in", values, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsNotIn(List<String> values) {
            addCriterion("channels not in", values, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsBetween(String value1, String value2) {
            addCriterion("channels between", value1, value2, "channels");
            return (Criteria) this;
        }

        public Criteria andChannelsNotBetween(String value1, String value2) {
            addCriterion("channels not between", value1, value2, "channels");
            return (Criteria) this;
        }

        public Criteria andOsIsNull() {
            addCriterion("os is null");
            return (Criteria) this;
        }

        public Criteria andOsIsNotNull() {
            addCriterion("os is not null");
            return (Criteria) this;
        }

        public Criteria andOsEqualTo(String value) {
            addCriterion("os =", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsNotEqualTo(String value) {
            addCriterion("os <>", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsGreaterThan(String value) {
            addCriterion("os >", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsGreaterThanOrEqualTo(String value) {
            addCriterion("os >=", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsLessThan(String value) {
            addCriterion("os <", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsLessThanOrEqualTo(String value) {
            addCriterion("os <=", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsLike(String value) {
            addCriterion("os like", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsNotLike(String value) {
            addCriterion("os not like", value, "os");
            return (Criteria) this;
        }

        public Criteria andOsIn(List<String> values) {
            addCriterion("os in", values, "os");
            return (Criteria) this;
        }

        public Criteria andOsNotIn(List<String> values) {
            addCriterion("os not in", values, "os");
            return (Criteria) this;
        }

        public Criteria andOsBetween(String value1, String value2) {
            addCriterion("os between", value1, value2, "os");
            return (Criteria) this;
        }

        public Criteria andOsNotBetween(String value1, String value2) {
            addCriterion("os not between", value1, value2, "os");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table avos_nexus
     *
     * @mbggenerated do_not_delete_during_merge Thu Nov 27 18:10:21 CST 2014
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}