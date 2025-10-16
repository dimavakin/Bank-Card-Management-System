package com.bankcards.controller;

import com.bankcards.dto.CardDto;
import com.bankcards.entity.CardStatus;
import com.bankcards.service.AdminCardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/card")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    AdminCardService adminCardService;

    @PostMapping("/{userId}")
    @Operation(summary = "Create card for user")
    public ResponseEntity<CardDto> createCard(@PathVariable Long userId) {
        CardDto card = adminCardService.createCard(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Block card")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long cardId) {
        CardDto card = adminCardService.updateCardStatus(cardId, CardStatus.BLOCKED);
        return ResponseEntity.ok(card);
    }

    @PatchMapping("/{cardId}/activate")
    @Operation(summary = "Activate card")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long cardId) {
        CardDto card = adminCardService.updateCardStatus(cardId, CardStatus.ACTIVE);
        return ResponseEntity.ok(card);
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Delete card")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all cards with filtering")
    public ResponseEntity<Page<CardDto>> getAllCards(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) CardStatus status) {

        Page<CardDto> cards = adminCardService.getAllCards(page, size, status);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user cards")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CardDto> cards = adminCardService.getCardsByUser(userId, page, size);
        return ResponseEntity.ok(cards);
    }
}
