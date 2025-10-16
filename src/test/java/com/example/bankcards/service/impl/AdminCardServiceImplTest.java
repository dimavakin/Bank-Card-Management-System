package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceImplTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardGenerator cardGenerator;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private AdminCardServiceImpl adminCardService;

    private User user;
    private Card card;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADMIN);
        user.setRoles(roles);

        card = new Card();
        card.setId(1L);
        card.setCardNumber("4111111111111111");
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);
        card.setCreatedAt(LocalDateTime.now());

        cardDto = new CardDto(
                1L, "**** **** **** 1234", "John Doe",
                LocalDate.now().plusYears(3), CardStatus.ACTIVE,
                BigDecimal.ZERO, LocalDateTime.now()
        );
    }

    @Test
    void createCard_Success_ReturnsCardDto() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardGenerator.generateCard(user)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        CardDto result = adminCardService.createCard(userId);

        assertEquals(cardDto, result);
        verify(userRepository).findById(userId);
        verify(cardGenerator).generateCard(user);
        verify(cardRepository).save(card);
        verify(cardMapper).mapCardToCardDto(card);
    }

    @Test
    void createCard_UserNotFound_ThrowsNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> adminCardService.createCard(userId));
        assertEquals("UserId: 999 not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(cardGenerator, never()).generateCard(any());
    }

    @Test
    void updateCardStatus_ActiveToBlocked_Success() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        CardDto result = adminCardService.updateCardStatus(cardId, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertEquals(cardDto, result);
        verify(cardRepository).findById(cardId);
        verify(cardRepository).save(card);
    }

    @Test
    void updateCardStatus_ExpiredToActive_ThrowsValidationException() {
        card.setStatus(CardStatus.EXPIRED);
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> adminCardService.updateCardStatus(cardId, CardStatus.ACTIVE));
        assertEquals("Cannot activate expired card", exception.getMessage());
        verify(cardRepository).findById(cardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateCardStatus_CardNotFound_ThrowsNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> adminCardService.updateCardStatus(cardId, CardStatus.BLOCKED));
        assertEquals("CardId: 999 not found", exception.getMessage());
    }

    @Test
    void deleteCard_ZeroBalance_Success() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        adminCardService.deleteCard(cardId);

        verify(cardRepository).findById(cardId);
        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_NonZeroBalance_ThrowsValidationException() {
        card.setBalance(new BigDecimal("100.00"));
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> adminCardService.deleteCard(cardId));
        assertEquals("Cannot delete card with non-zero balance", exception.getMessage());
        verify(cardRepository, never()).delete(any());
    }

    @Test
    void deleteCard_CardNotFound_ThrowsNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> adminCardService.deleteCard(cardId));
        assertEquals("CardId: 999 not found", exception.getMessage());
    }

    @Test
    void getAllCards_WithoutFilters_ReturnsPagedCards() {
        int page = 0;
        int size = 10;
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findAllWithFilters(isNull(), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        Page<CardDto> result = adminCardService.getAllCards(page, size, null);

        assertEquals(1, result.getContent().size());
        verify(cardRepository).findAllWithFilters(isNull(), any(Pageable.class));
    }

    @Test
    void getCardsByUser_Success_ReturnsUserCardsPage() {
        Long userId = 1L;
        int page = 0;
        int size = 10;
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        Page<CardDto> result = adminCardService.getCardsByUser(userId, page, size);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findById(userId);
        verify(cardRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    void getCardsByUser_UserNotFound_ThrowsNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> adminCardService.getCardsByUser(userId, 0, 10));
        assertEquals("UserId: 999 not found", exception.getMessage());
        verify(cardRepository, never()).findByUser(any(), any());
    }
}