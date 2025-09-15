package com.example.bankcards.dto;

import java.math.BigDecimal;

public class CardDTO {
    private Long id;
    private String maskedNumber;
    private String status;
    private BigDecimal balance;
    private Long ownerId;
    private String ownerUsername;

    public CardDTO() {}

    public CardDTO(Long id, String maskedNumber, String status, BigDecimal balance, Long ownerId, String ownerUsername) {
        this.id = id;
        this.maskedNumber = maskedNumber;
        this.status = status;
        this.balance = balance;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() { return id; }
    public String getMaskedNumber() { return maskedNumber; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
}
