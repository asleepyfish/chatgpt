package io.github.asleepyfish.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: asleepyfish
 * @Date: 2023/3/3 14:44
 * @Description: RoleEnum
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {
    /**
     * system
     */
    SYSTEM("system"),

    /**
     * assistant
     */
    ASSISTANT("assistant"),

    /**
     * user
     */
    USER("user");

    private final String roleName;
}
