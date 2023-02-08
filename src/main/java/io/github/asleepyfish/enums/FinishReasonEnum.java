package io.github.asleepyfish.enums;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-08 21:38
 * @Description: FinishReasonEnum
 */
public enum FinishReasonEnum {
    /**
     * length
     */
    LENGTH("length"),
    STOP("stop");
    private final String message;

    FinishReasonEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
