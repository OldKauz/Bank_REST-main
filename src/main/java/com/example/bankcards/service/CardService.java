package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public CardDTO createCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setOwner(user);
        card.setExpirationDate(java.time.LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        return new CardDTO(cardRepository.save(card));
    }

    public List<CardDTO> getUserCards(Long userId) {
        return cardRepository.findByOwnerId(userId).stream().map(CardDTO::new).collect(Collectors.toList());
    }

    public List<CardDTO> getAllCards() {
        return cardRepository.findAll().stream().map(CardDTO::new).collect(Collectors.toList());
    }

    public CardDTO deposit(Long cardId, BigDecimal amount) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Only active cards can be recharged");
        }
        card.setBalance(card.getBalance().add(amount));
        return new CardDTO(cardRepository.save(card));
    }

    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        Card from = cardRepository.findById(fromCardId).orElseThrow(() -> new RuntimeException("Source card not found"));
        Card to = cardRepository.findById(toCardId).orElseThrow(() -> new RuntimeException("Target card not found"));

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

        cardRepository.save(from);
        cardRepository.save(to);
    }

    // Пользователь запрашивает блокировку — ставим статус BLOCK_REQUESTED
    public CardDTO requestBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        if (!card.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("You can request block only for your own cards");
        }
        card.setStatus(CardStatus.BLOCK_REQUESTED);
        return new CardDTO(cardRepository.save(card));
    }

    // ADMIN: утвердить блокировку (или просто заблокировать)
    public CardDTO blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        return new CardDTO(cardRepository.save(card));
    }

    // ADMIN: активировать карту
    public CardDTO activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        return new CardDTO(cardRepository.save(card));
    }
}