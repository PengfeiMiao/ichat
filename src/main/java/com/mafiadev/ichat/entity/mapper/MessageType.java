package com.mafiadev.ichat.entity.mapper;

public enum MessageType {
    AI("AI"),
    USER("USER"),
    SYSTEM("SYSTEM");

    private final String name;

    MessageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MessageType of(String name) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getName().equalsIgnoreCase(name)) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("No matching enum constant for name: " + name);
    }
}