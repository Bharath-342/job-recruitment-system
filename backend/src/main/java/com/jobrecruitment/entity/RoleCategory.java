package com.jobrecruitment.entity;

public enum RoleCategory {
    JAVA_FULL_STACK("Java Full Stack"),
    JAVA_BACKEND("Java Backend"),
    JAVA_DEVELOPMENT("Java Development"),
    SOFTWARE_ENGINEERING("Software Engineering"),
    IT_SOFTWARE("IT & Software"),
    QA_AUTOMATION("QA & Automation"),
    IT_SUPPORT("Application Support"),
    OTHER("Other");

    private final String displayName;

    RoleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
