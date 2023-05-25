package io.github.asleepyfish.entity.billing;

import lombok.Data;

/**
 * @Author: asleepyfish
 * @Date: 2023/5/24 11:13
 * @Description: Billing
 */
@Data
public class Billing {

    private String dueDate;

    private String total;

    private String usage;

    private String balance;
}
