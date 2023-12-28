package io.github.asleepyfish.strategy;

import java.util.List;

/**
 * @Author: asleepyfish
 * @Date: 2023/12/27 17:49
 * @Description: TokenStrategy
 */
public interface TokenStrategy {

    /**
     * 获取token
     *
     * @param tokens token列表
     * @return {@link String}
     */
    String getToken(List<String> tokens);
}
