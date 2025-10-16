package com.example.bankcards.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.BlockCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.BlockedCardRequestRepository;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private BlockedCardRequestRepository blockedCardRequestRepository;

    @InjectMocks
    private UserCardServiceImpl userCardService;

    private User user;
    private Card card;
    private CardDto cardDto;
    private Page<Card> cardPage;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        card = new Card();
        card.setId(1L);
        card.setCardNumber("4111111111111111");
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);
        card.setCreatedAt(LocalDateTime.now());

        cardDto = new CardDto(
                1L, "**** **** **** 1111", "John Doe",
                LocalDate.now().plusYears(3), CardStatus.ACTIVE,
                BigDecimal.ZERO, LocalDateTime.now()
        );

        cardPage = new PageImpl<>(List.of(card));
    }

    @Test
    void getUserCards_WithoutFilters_ReturnsPagedCards() {
        int page = 0;
        int size = 10;
        when(cardRepository.findByUserWithFilters(eq(user.getEmail()), isNull(), any(Pageable.class)))
                .thenReturn(cardPage);
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        Page<CardDto> result = userCardService.getUserCardsByEmail(user.getEmail(), page, size, null, null);

        assertEquals(1, result.getContent().size());
        verify(cardRepository).findByUserWithFilters(eq(user.getEmail()), isNull(),
                argThat(pageable -> pageable.getSort().iterator().next().getProperty().equals("createdAt")));
    }

    @Test
    void getUserCards_WithStatusFilter_ReturnsFilteredCards() {
        int page = 0;
        int size = 5;
        CardStatus status = CardStatus.ACTIVE;
        when(cardRepository.findByUserWithFilters(eq(user.getEmail()), eq(status), any(Pageable.class)))
                .thenReturn(cardPage);
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        Page<CardDto> result = userCardService.getUserCardsByEmail(user.getEmail(), page, size, null, status);

        assertEquals(1, result.getContent().size());
        verify(cardRepository).findByUserWithFilters(eq(user.getEmail()), eq(status), any(Pageable.class));
    }

    @Test
    void getCardForUser_Success_ReturnsCardDto() {
        Long cardId = 1L;
        when(cardRepository.findByIdAndEmail(cardId, user.getEmail())).thenReturn(Optional.of(card));
        when(cardMapper.mapCardToCardDto(card)).thenReturn(cardDto);

        CardDto result = userCardService.getCardForUserByEmail(cardId, user.getEmail());

        assertEquals(cardDto, result);
        verify(cardRepository).findByIdAndEmail(cardId, user.getEmail());
        verify(cardMapper).mapCardToCardDto(card);
    }

    @Test
    void getCardForUser_CardNotFound_ThrowsNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findByIdAndEmail(cardId, user.getEmail())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userCardService.getCardForUserByEmail(cardId, user.getEmail()));
        assertEquals("CardId: 999 not found", exception.getMessage());
        verify(cardRepository).findByIdAndEmail(cardId, user.getEmail());
        verify(cardMapper, never()).mapCardToCardDto(any());
    }

    @Test
    void requestCardBlock_ActiveZeroBalance_Success() {
        Long cardId = 1L;
        when(cardRepository.findByIdAndEmail(cardId, user.getEmail())).thenReturn(Optional.of(card));
        when(blockedCardRequestRepository.save(any(BlockCardRequest.class)))
                .thenReturn(new BlockCardRequest());

        userCardService.requestCardBlockByEmail(cardId, user.getEmail());

        verify(cardRepository).findByIdAndEmail(cardId, user.getEmail());
        verify(blockedCardRequestRepository).save(argThat(request ->
                request.getCard().getId().equals(cardId)));
    }

    @Test
    void requestCardBlock_CardNotFound_ThrowsNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findByIdAndEmail(cardId, user.getEmail())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userCardService.requestCardBlockByEmail(cardId, user.getEmail()));
        assertEquals("CardId: 999 not found", exception.getMessage());
        verify(cardRepository).findByIdAndEmail(cardId, user.getEmail());
        verify(blockedCardRequestRepository, never()).save(any());
    }

    @Test
    void requestCardBlock_NonActiveCard_ThrowsValidationException() {
        Long cardId = 1L;
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndEmail(cardId, user.getEmail())).thenReturn(Optional.of(card));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userCardService.requestCardBlockByEmail(cardId, user.getEmail()));
        assertEquals("Only active cards can be blocked", exception.getMessage());
        verify(blockedCardRequestRepository, never()).save(any());
    }

    @Test
    void requestCardBlock_NonZeroBalance_ThrowsValidationException() {
        Long cardId = 1L;
        card.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findByIdAndEmail(cardId, user.getEmail())).thenReturn(Optional.of(card));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userCardService.requestCardBlockByEmail(cardId, user.getEmail()));
        assertEquals("Cannot block card with non-zero balance", exception.getMessage());
        verify(blockedCardRequestRepository, never()).save(any());
    }

    @Test
    void getUserCards_EmptyPage_ReturnsEmptyPage() {
        Page<Card> emptyPage = new PageImpl<>(List.of());
        when(cardRepository.findByUserWithFilters(eq(user.getEmail()), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<CardDto> result = userCardService.getUserCardsByEmail(user.getEmail(), 0, 10, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCardForUser_WrongUser_ThrowsNotFoundException() {
        Long cardId = 1L;
        User wrongUser = new User();
        wrongUser.setId(2L);
        when(cardRepository.findByIdAndEmail(cardId, wrongUser.getEmail())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userCardService.getCardForUserByEmail(cardId, wrongUser.getEmail()));
        assertEquals("CardId: 1 not found", exception.getMessage());
    }
}