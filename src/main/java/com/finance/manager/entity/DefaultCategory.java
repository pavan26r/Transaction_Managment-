package com.finance.manager.entity;

import lombok.Getter;

@Getter
public enum DefaultCategory {
    SALARY("Salary", CategoryType.INCOME),
    FOOD("Food", CategoryType.EXPENSE),
    RENT("Rent", CategoryType.EXPENSE),
    TRANSPORTATION("Transportation", CategoryType.EXPENSE),
    ENTERTAINMENT("Entertainment", CategoryType.EXPENSE),
    HEALTHCARE("Healthcare", CategoryType.EXPENSE),
    UTILITIES("Utilities", CategoryType.EXPENSE);

    private final String name;
    private final CategoryType type;

    DefaultCategory(String name, CategoryType type) {
        this.name = name;
        this.type = type;
    }

    public static boolean isDefault(String name) {
        for (DefaultCategory dc : values()) {
            if (dc.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static CategoryType getTypeByName(String name) {
        for (DefaultCategory dc : values()) {
            if (dc.getName().equalsIgnoreCase(name)) return dc.getType();
        }
        return null;
    }
}
