package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;

public interface AdminCardService {
    CardDto createCard(Long userId);

    CardDto updateCardStatus(Long cardId, CardStatus cardStatus);

    void deleteCard(Long cardId);

    Page<CardDto> getAllCards(int page, int size, CardStatus status);

    Page<CardDto> getCardsByUser(Long userId, int page, int size);
}
