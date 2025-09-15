package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.DepositRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardController cardController;

    private CardDTO cardDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardDTO = new CardDTO();
    }

    @Test
    void createCard_success() {
        when(cardService.createCard(1L, "1234")).thenReturn(cardDTO);

        ResponseEntity<CardDTO> response = cardController.createCard(1L, "1234");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(cardDTO, response.getBody());
    }

    @Test
    void getMyCards_success() {
        when(authentication.getName()).thenReturn("john");
        when(cardService.getCardsByUsername("john")).thenReturn(List.of(cardDTO));

        ResponseEntity<List<CardDTO>> response = cardController.getMyCards(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void deposit_success() {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));

        when(cardService.deposit(1L, BigDecimal.valueOf(100))).thenReturn(cardDTO);

        ResponseEntity<CardDTO> response = cardController.deposit(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(cardDTO, response.getBody());
    }

    @Test
    void transfer_success() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(50));

        ResponseEntity<String> response = cardController.transfer(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Перевод успешно выполнен", response.getBody());
    }
}
