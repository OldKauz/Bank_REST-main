package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import java.math.BigDecimal;

public class CardDTO {
    private Long id;
    private String maskedCardNumber;
    private String status;
    private BigDecimal balance;
    private Long ownerId;
    private String ownerUsername;

    public CardDTO(Card card) {
        this.id = card.getId();
        this.maskedCardNumber = maskCardNumber(card.getCardNumber());
        this.status = card.getStatus().toString();
        this.balance = card.getBalance();
        this.ownerId = card.getOwner().getId();
        this.ownerUsername = card.getOwner().getUsername();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // геттеры
    public Long getId() { return id; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
}
