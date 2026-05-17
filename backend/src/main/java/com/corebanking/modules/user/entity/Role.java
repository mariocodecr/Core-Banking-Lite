package com.corebanking.modules.user.entity;

public enum Role {
    ADMIN,    // Full system access
    AUDITOR,  // Read-only access to audit logs and financial data
    ADVISOR,  // Customer and account management
    CLIENT    // Own account access only
}
