package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardMaskingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardMapper {
    CardMaskingService cardMaskingService;

    public CardDto mapCardToCardDto(Card card) {
        if (card == null) {
            return null;
        }

        CardDto dto = new CardDto();

        dto.setId(card.getId());
        dto.setMaskedCardNumber(cardMaskingService.maskCardNumber(card.getCardNumber()));
        dto.setOwnerName(formatUserName(card.getUser()));
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setCreatedAt(card.getCreatedAt());

        return dto;
    }

    private String formatUserName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }
}
