package com.example.bankcards.service.impl;

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
import com.example.bankcards.service.UserCardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCardServiceImpl implements UserCardService {
    CardRepository cardRepository;
    CardMapper cardMapper;
    BlockedCardRequestRepository blockedCardRequestRepository;

    @Override
    public Page<CardDto> getUserCardsByEmail(String email, int page, int size, String search, CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Card> cards = cardRepository.findByUserWithFilters(email, status, pageable);

        return cards.map(cardMapper::mapCardToCardDto);
    }

    @Override
    public CardDto getCardForUserByEmail(Long cardId, String email) {
        Card card = cardRepository.findByIdAndEmail(cardId, email)
                .orElseThrow(() -> new NotFoundException(String.format("CardId: %s not found", cardId)));

        return cardMapper.mapCardToCardDto(card);
    }

    @Override
    public void requestCardBlockByEmail(Long cardId, String email) {
        Card card = cardRepository.findByIdAndEmail(cardId, email)
                .orElseThrow(() -> new NotFoundException(String.format("CardId: %s not found", cardId)));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ValidationException("Only active cards can be blocked");
        }

        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Cannot block card with non-zero balance");
        }

        BlockCardRequest blockCardRequest = new BlockCardRequest();
        blockCardRequest.setCard(card);

        blockedCardRequestRepository.save(blockCardRequest);
    }

    @Override
    public BigDecimal getBalanceForUserByEmail(String email) {
        return cardRepository.getBalanceByEmail(email)
                .orElse(BigDecimal.ZERO);
    }
}
