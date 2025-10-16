package com.bankcards.controller;

import com.bankcards.dto.TransferRequest;
import com.bankcards.entity.Role;
import com.bankcards.exception.InsufficientFundsException;
import com.bankcards.dto.TransferResponse;
import com.bankcards.entity.User;
import com.bankcards.exception.InvalidTransferAmountException;
import com.bankcards.exception.NotFoundException;
import com.bankcards.security.CustomUserDetails;
import com.bankcards.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController controller;

    @Mock
    private Authentication authentication;

    private TransferRequest request;
    private TransferResponse response;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Test");
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setEmail("email@test.test");

        CustomUserDetails userDetails = new CustomUserDetails(user);

        request = new TransferRequest();
        request.setSourceCardId(1L);
        request.setTargetCardId(2L);
        request.setAmount(new BigDecimal("200.00"));

        response = new TransferResponse();
        response.setSourceCardId(1L);
        response.setTargetCardId(2L);
        response.setAmount(new BigDecimal("200.00"));
        response.setCreatedAt(LocalDateTime.now());

        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void transferBetweenUserCards_Success_Returns200() {
        when(transferService.transferBetweenUserCards(request, "email@test.test"))
                .thenReturn(response);

        ResponseEntity<TransferResponse> result = controller.transferBetweenUserCards(request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(transferService).transferBetweenUserCards(request, "email@test.test");
    }

    @Test
    void transferBetweenUserCards_NotFound_ThrowsException() {
        when(transferService.transferBetweenUserCards(request, "email@test.test"))
                .thenThrow(new NotFoundException("Card not found"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> controller.transferBetweenUserCards(request, authentication));
        assertEquals("Card not found", exception.getMessage());
        verify(transferService).transferBetweenUserCards(request, "email@test.test");
    }

    @Test
    void transferBetweenUserCards_InsufficientFunds_ThrowsException() {
        when(transferService.transferBetweenUserCards(request, "email@test.test"))
                .thenThrow(new InsufficientFundsException(new BigDecimal("100.00"), new BigDecimal("200.00")));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> controller.transferBetweenUserCards(request, authentication));
        assertEquals("Insufficient funds. Available: 100.00, Required: 200.00", exception.getMessage());
        verify(transferService).transferBetweenUserCards(request, "email@test.test");
    }

    @Test
    void transferBetweenUserCards_InvalidAmount_ThrowsException() {
        when(transferService.transferBetweenUserCards(request, "email@test.test"))
                .thenThrow(new InvalidTransferAmountException("Transfer amount must be positive"));

        InvalidTransferAmountException exception = assertThrows(InvalidTransferAmountException.class,
                () -> controller.transferBetweenUserCards(request, authentication));
        assertEquals("Transfer amount must be positive", exception.getMessage());
        verify(transferService).transferBetweenUserCards(request, "email@test.test");
    }

}