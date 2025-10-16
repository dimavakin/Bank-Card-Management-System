package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.util.CardGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCardServiceImpl implements AdminCardService {
    CardRepository cardRepository;
    UserRepository userRepository;
    CardGenerator cardGenerator;
    CardMapper cardMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CardDto createCard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("UserId: %s not found", userId)));

        Card card = cardGenerator.generateCard(user);

        Card newCard = cardRepository.save(card);

        return cardMapper.mapCardToCardDto(newCard);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CardDto updateCardStatus(Long cardId, CardStatus cardStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(String.format("CardId: %s not found", cardId)));

        validateStatusTransition(card.getStatus(), cardStatus);

        card.setStatus(cardStatus);

        Card updatedCard = cardRepository.save(card);

        return cardMapper.mapCardToCardDto(updatedCard);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(String.format("CardId: %s not found", cardId)));

        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Cannot delete card with non-zero balance");
        }

        cardRepository.delete(card);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getAllCards(int page, int size, CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Card> cards = cardRepository.findAllWithFilters(status, pageable);

        return cards.map(cardMapper::mapCardToCardDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getCardsByUser(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("UserId: %s not found", userId)));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Card> cards = cardRepository.findByUser(user, pageable);

        return cards.map(cardMapper::mapCardToCardDto);
    }

    private void validateStatusTransition(CardStatus from, CardStatus to) {

        if (from == CardStatus.EXPIRED && to == CardStatus.ACTIVE) {
            throw new ValidationException("Cannot activate expired card");
        }
    }
}
