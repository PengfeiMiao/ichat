package com.mafiadev.ichat.llm.rag;

import com.mafiadev.ichat.util.ConfigUtil;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

public class RetrieverFactory {
    public static EmbeddingModel buildEmbeddingModel() {
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    public static WebSearchEngine buildWebSearchEngine() {
        return TavilyWebSearchEngine.builder()
                .apiKey(ConfigUtil.getConfig("tavily.key")) // get a free key: https://app.tavily.com/sign-in
                .build();
    }

    public static ContentRetriever buildEmbeddingStoreContentRetriever(int maxResult) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(CachedEmbeddingStore.getInstance())
                .embeddingModel(buildEmbeddingModel())
                .maxResults(maxResult)
                .minScore(0.6)
                .build();
    }

    public static ContentRetriever buildWebSearchContentRetriever(int maxResult) {
        return WebSearchContentRetriever.builder()
                .webSearchEngine(buildWebSearchEngine())
                .maxResults(maxResult)
                .build();
    }
}
