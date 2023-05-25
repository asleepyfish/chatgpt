package io.github.asleepyfish.entity.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: asleepyfish
 * @Date: 2023/5/23 17:46
 * @Description: Subscription
 */
@Data
public class Subscription {

    private String object;

    @JsonProperty("has_payment_method")
    private boolean hasPaymentMethod;

    private boolean canceled;

    @JsonProperty("canceled_at")
    private String canceledAt;

    private String delinquent;

    @JsonProperty("access_until")
    private long accessUntil;

    @JsonProperty("soft_limit")
    private long softLimit;

    @JsonProperty("hard_limit")
    private long hardLimit;

    @JsonProperty("system_hard_limit")
    private long systemHardLimit;

    @JsonProperty("soft_limit_usd")
    private String softLimitUsd;

    @JsonProperty("hard_limit_usd")
    private String hardLimitUsd;

    @JsonProperty("system_hard_limit_usd")
    private String systemHardLimitUsd;

    @JsonProperty("plan")
    private Plan plan;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("po_number")
    private long poNumber;

    @JsonProperty("billing_email")
    private String billingEmail;

    @JsonProperty("tax_ids")
    private String taxIds;

    @JsonProperty("billing_address")
    private String billingAddress;

    @JsonProperty("business_address")
    private String businessAddress;;
}
