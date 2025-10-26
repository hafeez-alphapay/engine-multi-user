package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum ApprovalStatus {

    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String name;

    ApprovalStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
