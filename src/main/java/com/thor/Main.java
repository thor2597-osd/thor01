package com.thor;

import com.thor.util.ModelUtils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

public class Main {
  public static void main(String[] args) {
    OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
        .apiKey(ModelUtils.OpenAiKey)
        .baseUrl(ModelUtils.OpenAiBaseUrl)
        .build();

    Embedding embedding =  embeddingModel.embed("你好").content();
  }
}
