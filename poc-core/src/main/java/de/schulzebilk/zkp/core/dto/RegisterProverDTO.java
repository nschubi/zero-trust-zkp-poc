package de.schulzebilk.zkp.core.dto;

import java.math.BigInteger;

public class RegisterProverDTO {
    private String proverId;
    private BigInteger proverKey;

    public RegisterProverDTO() {
    }

    public RegisterProverDTO(String proverId, BigInteger proverKey) {
        this.proverId = proverId;
        this.proverKey = proverKey;
    }

    public String getProverId() {
        return proverId;
    }

    public void setProverId(String proverId) {
        this.proverId = proverId;
    }

    public BigInteger getProverKey() {
        return proverKey;
    }

    public void setProverKey(BigInteger proverKey) {
        this.proverKey = proverKey;
    }
}
