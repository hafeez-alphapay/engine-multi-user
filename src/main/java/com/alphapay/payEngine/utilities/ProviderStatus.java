package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum ProviderStatus {

    ENABLED("Enabled"),
    DISABLED("Disabled");

    private final String name;

    ProviderStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
