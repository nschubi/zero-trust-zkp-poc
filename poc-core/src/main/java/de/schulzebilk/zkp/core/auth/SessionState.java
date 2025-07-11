package de.schulzebilk.zkp.core.auth;

public enum SessionState {
    INITIALIZED, WAITING_FOR_COMMITMENT,
    WAITING_FOR_RESPONSE, COMPLETED, FAILED, VERIFIED
}
