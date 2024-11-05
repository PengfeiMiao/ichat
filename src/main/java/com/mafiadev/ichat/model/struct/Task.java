package com.mafiadev.ichat.model.struct;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Task {
    String cronExpr;
    String type;
    String content;
    String createdTips;
    String triggerTips;
}