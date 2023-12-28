package io.github.asleepyfish.strategy;

import io.github.asleepyfish.enums.exception.ChatGPTErrorEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: asleepyfish
 * @Date: 2023/12/28 10:16
 * @Description: DefaultTokenStrategy
 */
public class DefaultTokenStrategy implements TokenStrategy {

    @Override
    public String getToken(List<String> tokens) {
        if (!CollectionUtils.isEmpty(tokens)) {
            return tokens.get(0);
        }
        throw new ChatGPTException(ChatGPTErrorEnum.NO_AVAILABLE_TOKEN_ERROR);
    }
}
