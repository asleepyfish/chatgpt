package io.github.asleepyfish.service.openai;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class ChatFunctionMixIn {

    @JsonSerialize(using = ChatFunctionParametersSerializer.class)
    abstract Class<?> getParametersClass();

}
