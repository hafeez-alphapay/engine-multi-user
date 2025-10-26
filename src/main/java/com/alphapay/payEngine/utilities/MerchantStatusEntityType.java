package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum MerchantStatusEntityType {

    USER_ACTIVATE_ACCOUNT("User Activate Account"),
    USER_LOCK_ACCOUNT("User Lock Account"),
    MANAGER_APPROVAL("Manager Approval"),
    ADMIN_APPROVAL("Admin Approval"),
    MBME_PAYMENT_PROVIDER("mbme Payment Provider"),
    MF_PAYMENT_PROVIDER("mf Payment Provider");

    private final String name;

    MerchantStatusEntityType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
