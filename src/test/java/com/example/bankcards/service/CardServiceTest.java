package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private CardService cardService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardService = new CardService(cardRepository, userRepository, encryptionUtil);

        user = new User("john", "pass", "USER");
        user.setId(1L);

        when(encryptionUtil.encrypt(anyString())).thenAnswer(inv -> "encrypted_" + inv.getArgument(0));
        when(encryptionUtil.decrypt(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().replace("encrypted_", ""));
    }

    @Test
    void createCard_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CardDTO dto = cardService.createCard(1L, "1234567812345678");

        assertEquals("john", dto.getOwnerUsername());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(BigDecimal.ZERO, dto.getBalance());
    }

    @Test
    void deposit_success() {
        Card card = new Card();
        card.setId(1L);
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card.setOwner(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDTO dto = cardService.deposit(1L, BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), dto.getBalance());
    }

    @Test
    void deposit_blockedCard_throws() {
        Card card = new Card();
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class,
                () -> cardService.deposit(1L, BigDecimal.TEN));
    }

    @Test
    void transfer_success() {
        Card from = new Card();
        from.setId(1L);
        from.setOwner(user);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(BigDecimal.valueOf(200));

        Card to = new Card();
        to.setId(2L);
        to.setOwner(user);
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(BigDecimal.valueOf(50));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        cardService.transfer(1L, 2L, BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100), from.getBalance());
        assertEquals(BigDecimal.valueOf(150), to.getBalance());
    }

    @Test
    void transfer_notEnoughFunds_throws() {
        Card from = new Card();
        from.setOwner(user);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(BigDecimal.valueOf(10));

        Card to = new Card();
        to.setOwner(user);
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(BigDecimal.ZERO);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(RuntimeException.class,
                () -> cardService.transfer(1L, 2L, BigDecimal.valueOf(100)));
    }

    @Test
    void requestBlock_success() {
        Card card = new Card();
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDTO dto = cardService.requestBlock(1L, "john");
        assertEquals("BLOCK_REQUESTED", dto.getStatus());
    }

    @Test
    void updateExpiredCards_setsExpired() {
        Card card = new Card();
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().minusDays(1));

        when(cardRepository.findAll()).thenReturn(List.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        cardService.updateExpiredCards();
        assertEquals(CardStatus.EXPIRED, card.getStatus());
    }
}
