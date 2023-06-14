package io.github.asleepyfish.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-08 21:38
 * @Description: FinishReasonEnum
 */
@Getter
@AllArgsConstructor
public enum FinishReasonEnum {
    /**
     * length
     */
    LENGTH("length"),
    STOP("stop");
    private final String message;
}
