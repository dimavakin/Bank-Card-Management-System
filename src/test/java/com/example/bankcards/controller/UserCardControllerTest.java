package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.UserCardService;
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
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCardControllerTest {

    @Mock
    private UserCardService cardService;

    @InjectMocks
    private UserCardController controller;

    @Mock
    private Authentication authentication;

    private CardDto cardDto;
    private Page<CardDto> cardPage;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Test");
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setEmail("email@test.test");

        CustomUserDetails userDetails = new CustomUserDetails(user);

        cardDto = new CardDto(
                1L,
                "**** **** **** 1234",
                "Test Test",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00"),
                LocalDateTime.now()
        );

        cardPage = new PageImpl<>(List.of(cardDto));

        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void getMyCards_Success_ReturnsPage() {
        when(cardService.getUserCardsByEmail("email@test.test", 0, 10, null, null))
                .thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = controller.getMyCards(0, 10, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getContent().get(0).getId());
        verify(cardService).getUserCardsByEmail("email@test.test", 0, 10, null, null);
    }

    @Test
    void getMyCards_WithFilters_ReturnsFilteredPage() {
        when(cardService.getUserCardsByEmail("email@test.test", 0, 5, "1234", CardStatus.ACTIVE))
                .thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = controller.getMyCards(0, 5, "1234", CardStatus.ACTIVE, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cardService).getUserCardsByEmail("email@test.test", 0, 5, "1234", CardStatus.ACTIVE);
    }

    @Test
    void getUserCard_Success_ReturnsCard() {
        when(cardService.getCardForUserByEmail(1L, "email@test.test")).thenReturn(cardDto);

        ResponseEntity<CardDto> response = controller.getUserCard(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        verify(cardService).getCardForUserByEmail(1L, "email@test.test");
    }

    @Test
    void getUserCard_NotFound_ThrowsException() {
        when(cardService.getCardForUserByEmail(999L, "email@test.test"))
                .thenThrow(new NotFoundException("Card not found"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> controller.getUserCard(999L, authentication));
        assertEquals("Card not found", exception.getMessage());
        verify(cardService).getCardForUserByEmail(999L, "email@test.test");
    }

    @Test
    void requestCardBlock_Success_Returns202() {
        doNothing().when(cardService).requestCardBlockByEmail(1L, "email@test.test");

        ResponseEntity<Void> response = controller.requestCardBlock(1L, authentication);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(cardService).requestCardBlockByEmail(1L, "email@test.test");
    }

    @Test
    void requestCardBlock_NotFound_ThrowsException() {
        doThrow(new NotFoundException("Card not found"))
                .when(cardService).requestCardBlockByEmail(999L, "email@test.test");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> controller.requestCardBlock(999L, authentication));
        assertEquals("Card not found", exception.getMessage());
        verify(cardService).requestCardBlockByEmail(999L, "email@test.test");
    }

    @Test
    void getUserBalance_Success_ReturnsBalance() {
        BigDecimal balance = new BigDecimal("2500.00");
        when(cardService.getBalanceForUserByEmail("email@test.test")).thenReturn(balance);

        ResponseEntity<BigDecimal> response = controller.getUserBalance(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(balance, response.getBody());
        verify(cardService).getBalanceForUserByEmail("email@test.test");
    }

    @Test
    void getUserBalance_Null_ReturnsZero() {
        when(cardService.getBalanceForUserByEmail("email@test.test")).thenReturn(null);

        ResponseEntity<BigDecimal> response = controller.getUserBalance(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.ZERO, response.getBody());
        verify(cardService).getBalanceForUserByEmail("email@test.test");
    }

    @Test
    void getMyCards_EmptyPage_ReturnsEmpty() {
        Page<CardDto> emptyPage = new PageImpl<>(List.of());
        when(cardService.getUserCardsByEmail("email@test.test", 0, 10, null, null))
                .thenReturn(emptyPage);

        ResponseEntity<Page<CardDto>> response = controller.getMyCards(0, 10, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getContent().isEmpty());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    void getMyCards_Page1_ReturnsCorrectPage() {
        when(cardService.getUserCardsByEmail("email@test.test", 1, 20, null, null))
                .thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = controller.getMyCards(1, 20, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cardService).getUserCardsByEmail("email@test.test", 1, 20, null, null);
    }

    @Test
    void getMyCards_StatusBlocked_ReturnsFiltered() {
        when(cardService.getUserCardsByEmail("email@test.test", 0, 10, null, CardStatus.BLOCKED))
                .thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = controller.getMyCards(0, 10, null, CardStatus.BLOCKED, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cardService).getUserCardsByEmail("email@test.test", 0, 10, null, CardStatus.BLOCKED);
    }

    @Test
    void getMyCards_SearchFilter_ReturnsFiltered() {
        when(cardService.getUserCardsByEmail("email@test.test", 0, 10, "1234", null))
                .thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = controller.getMyCards(0, 10, "1234", null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cardService).getUserCardsByEmail("email@test.test", 0, 10, "1234", null);
    }

    @Test
    void getUserCard_DifferentUser_ThrowsException() {
        when(cardService.getCardForUserByEmail(1L, "test@example.com"))
                .thenThrow(new NotFoundException("Card not found"));

        User newUser = new User();
        newUser.setEmail("test@example.com");
        CustomUserDetails testDetails = new CustomUserDetails(newUser);

        when(authentication.getPrincipal()).thenReturn(testDetails);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> controller.getUserCard(1L, authentication));
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    void requestCardBlock_Card2_Success() {
        doNothing().when(cardService).requestCardBlockByEmail(2L, "email@test.test");

        ResponseEntity<Void> response = controller.requestCardBlock(2L, authentication);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(cardService).requestCardBlockByEmail(2L, "email@test.test");
    }

    @Test
    void getUserBalance_1000_ReturnsCorrect() {
        BigDecimal balance = new BigDecimal("1000.00");
        when(cardService.getBalanceForUserByEmail("email@test.test")).thenReturn(balance);

        ResponseEntity<BigDecimal> response = controller.getUserBalance(authentication);

        assertEquals(balance, response.getBody());
    }

    @Test
    void allMethods_CalledOnce() {
        when(cardService.getUserCardsByEmail(anyString(), anyInt(), anyInt(), any(), any())).thenReturn(cardPage);
        when(cardService.getCardForUserByEmail(anyLong(), anyString())).thenReturn(cardDto);
        when(cardService.getBalanceForUserByEmail(anyString())).thenReturn(new BigDecimal("1000"));
        doNothing().when(cardService).requestCardBlockByEmail(anyLong(), anyString());

        controller.getMyCards(0, 10, null, null, authentication);
        controller.getUserCard(1L, authentication);
        controller.requestCardBlock(1L, authentication);
        controller.getUserBalance(authentication);

        verify(cardService, times(1)).getUserCardsByEmail(anyString(), anyInt(), anyInt(), any(), any());
        verify(cardService, times(1)).getCardForUserByEmail(anyLong(), anyString());
        verify(cardService, times(1)).requestCardBlockByEmail(anyLong(), anyString());
        verify(cardService, times(1)).getBalanceForUserByEmail(anyString());
    }
}