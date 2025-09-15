package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.DepositRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Карты", description = "Управление банковскими картами")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Создать новую карту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка при создании карты")
    })
    @PostMapping("/create")
    public ResponseEntity<CardDTO> createCard(
            @RequestParam("userId") Long userId,
            @RequestParam("cardNumber") String cardNumber
    ) {
        return ResponseEntity.ok(cardService.createCard(userId, cardNumber));
    }

    @Operation(summary = "Посмотреть все карты пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты найдены"),
            @ApiResponse(responseCode = "404", description = "Карты не найдены")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDTO>> getUserCards(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(cardService.getUserCards(userId));
    }

    @Operation(
            summary = "Пополнение карты",
            description = "Позволяет пополнить баланс карты по её ID. Сумма должна быть больше 0."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное пополнение",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или бизнес-логики",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content)
    })
    @PostMapping("/{cardId}/deposit")
    public ResponseEntity<CardDTO> deposit(
            @PathVariable("cardId") Long cardId,
            @Valid @RequestBody DepositRequest request
    ) {
        return ResponseEntity.ok(cardService.deposit(cardId, request.getAmount()));
    }

    @Operation(
            summary = "Перевод между картами",
            description = "Переводит средства между картами одного пользователя. " +
                    "Сумма должна быть больше 0 и не превышать баланс отправителя."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или неверные параметры",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
        cardService.transfer(request.getFromCardId(), request.getToCardId(), request.getAmount());
        return ResponseEntity.ok("Перевод успешно выполнен");
    }

    // USER: запрос блoкировки (ставим BLOCK_REQUESTED)
    @PostMapping("/{cardId}/request-block")
    public ResponseEntity<CardDTO> requestBlock(
            @PathVariable("cardId") Long cardId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(cardService.requestBlock(cardId, username));
    }

    @Operation(summary = "Заблокировать карту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardDTO> blockCard(@PathVariable("cardId") Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    // ADMIN: активация карты
    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<CardDTO> activateCard(@PathVariable("cardId") Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }
}
