package com.ttn.ai.core.services.dto;

/**
 * Assignable AEM user for ticket assignment UI.
 */
public class AssignableUserDto {

    private String id;
    private String displayName;

    public AssignableUserDto(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
