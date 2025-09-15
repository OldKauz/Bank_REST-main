package com.example.bankcards.entity;

import com.example.bankcards.util.EncryptionUtil;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number_encrypted", nullable = false, unique = true)
    private String cardNumberEncrypted;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status; // enum

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Transient
    private EncryptionUtil encryptionUtil;

    public Card() {};

    public Card(Long id, String cardNumberEncrypted, LocalDate expirationDate, CardStatus status, BigDecimal balance, User owner) {
        this.id = id;
        this.cardNumberEncrypted = cardNumberEncrypted;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.owner = owner;
    }

    public void setEncryptionUtil(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    public void setCardNumberPlain(String plain) {
        if (encryptionUtil == null) {
            throw new IllegalStateException("EncryptionUtil not set");
        }
        this.cardNumberEncrypted = encryptionUtil.encrypt(plain);
    }

    public Long getId() {
        return id;
    }

    public String getCardNumberEncrypted() {
        return cardNumberEncrypted;
    }

    public void setCardNumberEncrypted(String cardNumberEncrypted) {
        this.cardNumberEncrypted = cardNumberEncrypted;
    }

    public String getMaskedNumber(EncryptionUtil encryptionUtil) {
        if (cardNumberEncrypted == null) return null;
        String decrypted = encryptionUtil.decrypt(cardNumberEncrypted);
        return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

