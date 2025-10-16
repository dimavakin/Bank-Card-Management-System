package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api/user/transfer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferController {
    TransferService transferService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Transfer between user cards")
    public ResponseEntity<TransferResponse> transferBetweenUserCards(
            @Valid @RequestBody TransferRequest transferRequest,
            Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        TransferResponse transferResponse = transferService.transferBetweenUserCards(transferRequest, details.getUsername());
        return ResponseEntity.ok(transferResponse);
    }
}
