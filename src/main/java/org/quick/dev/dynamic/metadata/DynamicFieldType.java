package org.quick.dev.dynamic.metadata;

public enum DynamicFieldType {

    KEY("KEY"), STRING("STRING"), ENUM("ENUM"),
    TEXTAREA("TEXTAREA"), RICH_TEXT("RICH_TEXT"), INTEGER("INTEGER"),
    BOOL("BOOL"), DATETIME("DATETIME"),
    FOREIGN_KEY("FOREIGN_KEY");

    private String name;

    DynamicFieldType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
