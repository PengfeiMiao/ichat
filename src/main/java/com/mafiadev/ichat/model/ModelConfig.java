package com.mafiadev.ichat.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModelConfig {
    private String name;
    private String baseUrl;
    private String apiKey;
    private String type;
}
