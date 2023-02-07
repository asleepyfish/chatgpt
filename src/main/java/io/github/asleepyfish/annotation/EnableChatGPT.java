package io.github.asleepyfish.annotation;

import io.github.asleepyfish.config.ChatGPTAutoConfigure;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-07 23:48
 * @Description: enable annotation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ChatGPTAutoConfigure.class)
@Inherited
public @interface EnableChatGPT {
}
