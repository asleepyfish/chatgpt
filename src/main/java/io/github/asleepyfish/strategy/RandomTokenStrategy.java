package io.github.asleepyfish.strategy;

import io.github.asleepyfish.enums.exception.ChatGPTErrorEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * @Author: asleepyfish
 * @Date: 2023/12/28 10:28
 * @Description: RandomTokenStrategy
 */
public class RandomTokenStrategy implements TokenStrategy {

    private static final Random r = new Random();

    @Override
    public String getToken(List<String> tokens) {
        if (!CollectionUtils.isEmpty(tokens)) {
            return tokens.get(r.nextInt(tokens.size()));
        }
        throw new ChatGPTException(ChatGPTErrorEnum.NO_AVAILABLE_TOKEN_ERROR);
    }
}
