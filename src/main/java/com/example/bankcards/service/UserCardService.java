package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface UserCardService {
    Page<CardDto> getUserCardsByEmail(String email, int page, int size, String search, CardStatus status);

    CardDto getCardForUserByEmail(Long cardId, String email);

    void requestCardBlockByEmail(Long cardId, String email);

    BigDecimal getBalanceForUserByEmail(String email);
}
