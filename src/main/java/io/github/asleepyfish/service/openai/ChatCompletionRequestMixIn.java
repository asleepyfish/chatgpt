package io.github.asleepyfish.service.openai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;

/**
 * @author asleepyfish
 */
public abstract class ChatCompletionRequestMixIn {

    @JsonSerialize(using = ChatCompletionRequestSerializerAndDeserializer.Serializer.class)
    @JsonDeserialize(using = ChatCompletionRequestSerializerAndDeserializer.Deserializer.class)
    abstract ChatCompletionRequest.ChatCompletionRequestFunctionCall getFunctionCall();

}
