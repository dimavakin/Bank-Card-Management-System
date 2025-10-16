package com.bankcards.controller;

import com.bankcards.dto.CardDto;
import com.bankcards.entity.CardStatus;
import com.bankcards.service.AdminCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
class AdminCardControllerTest {
    @Mock
    private AdminCardService adminCardService;
    @InjectMocks
    private AdminCardController adminCardController;

    private CardDto cardDto;


    @BeforeEach
    void setUp() {
        cardDto = new CardDto(
                1L,
                "1234********5678",
                "John Doe",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00"),
                LocalDateTime.now()
        );
    }

    @Test
    void createCard_Success_ReturnsCreatedCard() {
        Long userId = 1L;
        when(adminCardService.createCard(userId)).thenReturn(cardDto);

        ResponseEntity<CardDto> response = adminCardController.createCard(userId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(cardDto, response.getBody());
        verify(adminCardService).createCard(userId);
    }

    @Test
    void blockCard_Success_ReturnsBlockedCard() {
        Long cardId = 1L;
        CardDto blockedCard = new CardDto(
                1L,
                "1234********5678",
                "John Doe",
                LocalDate.now().plusYears(3),
                CardStatus.BLOCKED,
                new BigDecimal("1000.00"),
                LocalDateTime.now());
        when(adminCardService.updateCardStatus(cardId, CardStatus.BLOCKED)).thenReturn(blockedCard);

        ResponseEntity<CardDto> response = adminCardController.blockCard(cardId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CardStatus.BLOCKED, response.getBody().getStatus());
        verify(adminCardService).updateCardStatus(cardId, CardStatus.BLOCKED);
    }

    @Test
    void deleteCard_Success_ReturnsNoContent() {
        Long cardId = 1L;
        doNothing().when(adminCardService).deleteCard(cardId);

        ResponseEntity<Void> response = adminCardController.deleteCard(cardId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminCardService).deleteCard(cardId);
    }
    @Test
    void getAllCards_WithoutFilters_ReturnsPagedCards() {
        int page = 0;
        int size = 10;
        Page<CardDto> expectedPage = new PageImpl<>(List.of(cardDto));
        when(adminCardService.getAllCards(page, size, null)).thenReturn(expectedPage);

        ResponseEntity<Page<CardDto>> response = adminCardController.getAllCards(page, size, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        verify(adminCardService).getAllCards(page, size, null);
    }
    @Test
    void getUserCards_Success_ReturnsUserCardsPage() {
        Long userId = 1L;
        int page = 0;
        int size = 10;
        Page<CardDto> expectedPage = new PageImpl<>(List.of(cardDto));
        when(adminCardService.getCardsByUser(userId, page, size)).thenReturn(expectedPage);

        ResponseEntity<Page<CardDto>> response = adminCardController.getUserCards(userId, page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        verify(adminCardService).getCardsByUser(userId, page, size);
    }
}
