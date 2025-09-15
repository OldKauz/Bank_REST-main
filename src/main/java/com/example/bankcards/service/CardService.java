package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public CardService(CardRepository cardRepository, UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public CardDTO createCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Card card = new Card();
        card.setEncryptionUtil(encryptionUtil);
        card.setCardNumberPlain(cardNumber);
        card.setCardNumberEncrypted(encryptionUtil.encrypt(cardNumber));
        card.setOwner(user);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return toDto(cardRepository.save(card));
    }

    public List<CardDTO> getUserCards(Long userId) {
        return cardRepository.findByOwnerId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<CardDTO> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public CardDTO deposit(Long cardId, BigDecimal amount) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Only active cards can be recharged");
        }
        card.setBalance(card.getBalance().add(amount));
        return toDto(cardRepository.save(card));
    }

    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        Card from = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card to = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Target card not found"));

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Both cards must be ACTIVE for transfer");
        }

        if (!from.getOwner().getId().equals(to.getOwner().getId())) {
            throw new RuntimeException("You can only transfer between your own cards");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Not enough funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }

    // USER: запрос блокировки
    public CardDTO requestBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        if (!card.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("You can request block only for your own cards");
        }
        card.setStatus(CardStatus.BLOCK_REQUESTED);
        return toDto(cardRepository.save(card));
    }

    // ADMIN: блокировка
    public CardDTO blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        return toDto(cardRepository.save(card));
    }

    // ADMIN: активация
    public CardDTO activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        return toDto(cardRepository.save(card));
    }

    // USER: свои карты
    public List<CardDTO> getCardsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardRepository.findByOwner(user).stream()
                .map(this::toDto)
                .toList();
    }

    // Удаление карты
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new RuntimeException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    // Получение всех карт
    public List<CardDTO> getAllCards(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null) {
            CardStatus cardStatus;
            try {
                cardStatus = CardStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
            return cardRepository.findByStatus(cardStatus, pageable)
                    .stream()
                    .map(this::toDto)
                    .toList();
        } else {
            return cardRepository.findAll(pageable)
                    .stream()
                    .map(this::toDto)
                    .toList();
        }
    }

    // Посмотреть баланс
    public BigDecimal getBalance(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return card.getBalance();
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // раз в день в полночь
    public void updateExpiredCards() {
        List<Card> cards = cardRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Card card : cards) {
            if (card.getExpirationDate().isBefore(today) && card.getStatus() != CardStatus.EXPIRED) {
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
            }
        }
    }

    private CardDTO toDto(Card card) {
        return new CardDTO(
                card.getId(),
                card.getMaskedNumber(encryptionUtil), // маска готовится здесь
                card.getStatus().toString(),
                card.getBalance(),
                card.getOwner().getId(),
                card.getOwner().getUsername()
        );
    }
}