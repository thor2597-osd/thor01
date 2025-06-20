package com.secondhand.trading.util;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class ModelUtils {
  public static final String OpenAiKey = "sk-t8jjm5retpgieh92shqch06i5tmf4vjaojis2leo54s6fte7";
  public static final String OpenAiBaseUrl = "https://api.aihao123.cn/luomacode-api/open-api/v1";
  public static ChatLanguageModel getOpenAiModel(){
    return OpenAiChatModel.builder().apiKey(OpenAiKey).modelName("gpt-4o-mini").baseUrl(OpenAiBaseUrl).build();
  }
}
