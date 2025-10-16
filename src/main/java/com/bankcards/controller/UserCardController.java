package com.bankcards.controller;

import com.bankcards.service.UserCardService;
import com.bankcards.dto.CardDto;
import com.bankcards.entity.CardStatus;
import com.bankcards.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCardController {
    UserCardService cardService;

    @GetMapping
    @Operation(summary = "Get user's cards with filtering")
    public ResponseEntity<Page<CardDto>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        Page<CardDto> cards = cardService.getUserCardsByEmail(details.getUsername(), page, size, search, status);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Get specific user card")
    public ResponseEntity<CardDto> getUserCard(@PathVariable Long cardId, Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        CardDto card = cardService.getCardForUserByEmail(cardId, details.getUsername());
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{cardId}/block-request")
    @Operation(summary = "Request card blocking")
    public ResponseEntity<Void> requestCardBlock(@PathVariable Long cardId, Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        cardService.requestCardBlockByEmail(cardId, details.getUsername());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/balance")
    @Operation(summary = "Get total balance of all cards")
    public ResponseEntity<BigDecimal> getUserBalance(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        BigDecimal balance = cardService.getBalanceForUserByEmail(details.getUsername());
        return ResponseEntity.ok(balance != null ? balance : BigDecimal.ZERO);
    }
}
