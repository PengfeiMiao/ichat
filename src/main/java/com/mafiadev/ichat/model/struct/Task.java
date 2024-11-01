package com.mafiadev.ichat.model.struct;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Task {
    String cronExpression;
    String createdTips;
    String triggerTips;
}