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

import java.math.BigDecimal;
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

    // Создать карту
    @Operation(summary = "Создать новую карту (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта создана",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка при создании карты",
                    content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<CardDTO> createCard(
            @RequestParam("userId") Long userId,
            @RequestParam("cardNumber") String cardNumber
    ) {
        return ResponseEntity.ok(cardService.createCard(userId, cardNumber));
    }

    // Посмотреть мои карты USER
    @Operation(summary = "Посмотреть свои карты (USER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты пользователя найдены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class)))
    })
    @GetMapping("/my")
    public ResponseEntity<List<CardDTO>> getMyCards(Authentication authentication) {
        String username = authentication.getName();
        List<CardDTO> cards = cardService.getCardsByUsername(username);
        return ResponseEntity.ok(cards);
    }

    // Посмотреть все карты пользователя. ADMIN
    @Operation(summary = "Посмотреть карты конкретного пользователя (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты найдены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class))),
            @ApiResponse(responseCode = "404", description = "Карты не найдены",
                    content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDTO>> getUserCards(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(cardService.getUserCards(userId));
    }

    // Пополнить карту
    @Operation(
            summary = "Пополнение карты (USER)",
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

    // Перевести на карту
    @Operation(
            summary = "Перевод между картами (USER)",
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

    // USER: запрос блoкировки (BLOCK_REQUESTED)
    @Operation(summary = "Запросить блокировку карты (USER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос на блокировку создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class))),
            @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    @PostMapping("/{cardId}/request-block")
    public ResponseEntity<CardDTO> requestBlock(
            @PathVariable("cardId") Long cardId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(cardService.requestBlock(cardId, username));
    }

    // Блокнуть карту
    @Operation(summary = "Заблокировать карту (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта заблокирована",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardDTO> blockCard(@PathVariable("cardId") Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    // Активировать карту
    @Operation(summary = "Активировать карту (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта активирована",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDTO.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<CardDTO> activateCard(@PathVariable("cardId") Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    // Удалить карту (ADMIN)
    @Operation(summary = "Удалить карту (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Карта успешно удалена");
    }

    // Фильтр пагинация
    @Operation(summary = "Получить список всех карт с фильтрацией и пагинацией (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты найдены")
    })
    @GetMapping("/all")
    public ResponseEntity<List<CardDTO>> getAllCards(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "status", required = false) String status
    ) {
        return ResponseEntity.ok(cardService.getAllCards(page, size, status));
    }


    // Баланс
    @Operation(summary = "Посмотреть баланс карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс получен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable("cardId") Long cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }
}
