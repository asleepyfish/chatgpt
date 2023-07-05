package io.github.asleepyfish.entity.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: asleepyfish
 * @Date: 2023/7/5 13:30
 * @Description: BillingUsage
 */
@Data
public class BillingUsage {

    private String object;

    @JsonProperty("daily_costs")
    private DailyCost[] dailyCosts;

    @JsonProperty("total_usage")
    private String totalUsage;

    @Data
    public static class DailyCost {

        private long timestamp;

        @JsonProperty("line_items")
        private LineItem[] lineItems;
    }

    @Data
    public static class LineItem {

        private String name;

        private String usage;
    }
}
