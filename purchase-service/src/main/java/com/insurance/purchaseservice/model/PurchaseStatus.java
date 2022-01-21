package com.insurance.purchaseservice.model;

import java.io.Serializable;

public enum PurchaseStatus implements Serializable {
    PENDING,
    USER_CONFIRMED,
    POLICY_CONFIRMED,
    PRICE_CALCULATED,
    CONFIRMED,
    REJECTED
}
