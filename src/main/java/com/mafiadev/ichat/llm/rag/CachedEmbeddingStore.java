package com.mafiadev.ichat.llm.rag;

import com.mafiadev.ichat.llm.GptService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.Map;

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

    public static void addDocument(String text, Map<String, Object> metadata) {
        InMemoryEmbeddingStore<TextSegment> embeddingStore = getInstance();
        Document document = Document.document(text, Metadata.from(metadata));
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
    }

    public static void removeDocument(String key, String value) {
        InMemoryEmbeddingStore<TextSegment> embeddingStore = getInstance();
        embeddingStore.removeAll(it -> value.equals(((Metadata) it).getString(key)));
    }
}
