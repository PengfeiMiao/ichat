package com.mafiadev.ichat.llm.rag;

import com.mafiadev.ichat.llm.GptService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.Collections;

public class CachedEmbeddingStore {
    private static volatile InMemoryEmbeddingStore<TextSegment> instance;

    public static InMemoryEmbeddingStore<TextSegment> getInstance() {
        if (instance == null) {
            synchronized (GptService.class) {
                if (instance == null) {
                    instance = new InMemoryEmbeddingStore<>();
                }
            }
        }
        return instance;
    }

    public static void addDocument(String text) {
        InMemoryEmbeddingStore<TextSegment> embeddingStore = getInstance();
        EmbeddingStoreIngestor.ingest(Collections.singletonList(Document.document(text)), embeddingStore);
    }
}
