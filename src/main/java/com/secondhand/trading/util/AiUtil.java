package com.secondhand.trading.util;

import com.secondhand.trading.model.Result;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class AiUtil {
  // 创建记忆内容
  private final ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
  // 获取指定的openai模型
  private final ChatLanguageModel model = ModelUtils.getOpenAiModel();
  interface Assistant {
    String chat(@MemoryId long userId, @UserMessage String message);
  }
  @Tool
  public static String getTodayTime(){
    return LocalDateTime.now().toString();
  }

  public Result getMessageByAi(long userId,String message){
    // 获取工具类
    ToolSpecification specification;
    try {
      specification = ToolSpecifications.toolSpecificationFrom(AiUtil.class.getMethod("getTodayTime"));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // 获取接口实例
    Assistant assistant = AiServices.builder(AiUtil.Assistant.class)
        .chatLanguageModel(model)
        .chatMemoryProvider(memoryId -> chatMemory)
        .tools(specification)
        .build();
    // 返回结果
    return Result.success(assistant.chat(userId,message));
  }
}
