package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum UserAccountStatus {

    LOCKED("Locked"),
    UNLOCKED("Unlocked"),
    ENABLED("Enabled"),
    DISABLED("Disabled");

    private final String name;

    UserAccountStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
