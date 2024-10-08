package com.mafiadev.ichat.db;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataField {
    String name;
    String type;
}
