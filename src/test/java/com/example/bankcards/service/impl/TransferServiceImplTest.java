package com.example.bankcards.service.impl;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidTransferAmountException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.SameCardTransferException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User user;
    private Card sourceCard;
    private Card targetCard;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        sourceCard = new Card();
        sourceCard.setId(1L);
        sourceCard.setUser(user);
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setBalance(new BigDecimal("1000.00"));

        targetCard = new Card();
        targetCard.setId(2L);
        targetCard.setUser(user);
        targetCard.setStatus(CardStatus.ACTIVE);
        targetCard.setBalance(new BigDecimal("500.00"));

        request = new TransferRequest();
        request.setSourceCardId(1L);
        request.setTargetCardId(2L);
        request.setAmount(new BigDecimal("200.00"));
    }

    @Test
    void transferBetweenUserCards_Success() {
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.of(targetCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));


        TransferResponse response = transferService.transferBetweenUserCards(request, user.getEmail());
        assertEquals(1L, response.getSourceCardId());
        assertEquals(2L, response.getTargetCardId());
        assertEquals(new BigDecimal("200.00"), response.getAmount());
        assertTrue(response.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));

        assertEquals(new BigDecimal("800.00"), sourceCard.getBalance());
        assertEquals(new BigDecimal("700.00"), targetCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenUserCards_SourceCardNotFound_ThrowsNotFoundException() {
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Source card not found: 1", exception.getMessage());
        verify(cardRepository).findByIdAndEmail(1L, user.getEmail());
        verify(cardRepository, never()).findByIdAndEmail(2L, user.getEmail());
    }

    @Test
    void transferBetweenUserCards_TargetCardNotFound_ThrowsNotFoundException() {
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Target card not found: 2", exception.getMessage());
    }

    @Test
    void transferBetweenUserCards_SameCards_ThrowsSameCardTransferException() {
        request.setTargetCardId(1L);
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));

        SameCardTransferException exception = assertThrows(SameCardTransferException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Source and target cards must be different", exception.getMessage());
    }

    @Test
    void transferBetweenUserCards_SourceNotActive_ThrowsCardNotActiveException() {
        sourceCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.of(targetCard));

        CardNotActiveException exception = assertThrows(CardNotActiveException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Both cards must be active", exception.getMessage());
    }

    @Test
    void transferBetweenUserCards_TargetNotActive_ThrowsCardNotActiveException() {
        targetCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.of(targetCard));

        CardNotActiveException exception = assertThrows(CardNotActiveException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Both cards must be active", exception.getMessage());
    }

    @Test
    void transferBetweenUserCards_InsufficientFunds_ThrowsInsufficientFundsException() {
        sourceCard.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.of(targetCard));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Insufficient funds. Available: 100.00, Required: 200.00", exception.getMessage());
    }

    @Test
    void transferBetweenUserCards_ZeroAmount_ThrowsInvalidTransferAmountException() {
        request.setAmount(BigDecimal.ZERO);
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.of(targetCard));

        InvalidTransferAmountException exception = assertThrows(InvalidTransferAmountException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Transfer amount must be positive", exception.getMessage());
    }

    @Test
    void transferBetweenUserCards_NegativeAmount_ThrowsInvalidTransferAmountException() {
        request.setAmount(new BigDecimal("-50.00"));
        when(cardRepository.findByIdAndEmail(1L, user.getEmail())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndEmail(2L, user.getEmail())).thenReturn(Optional.of(targetCard));

        InvalidTransferAmountException exception = assertThrows(InvalidTransferAmountException.class,
                () -> transferService.transferBetweenUserCards(request, user.getEmail()));
        assertEquals("Transfer amount must be positive", exception.getMessage());
    }

}