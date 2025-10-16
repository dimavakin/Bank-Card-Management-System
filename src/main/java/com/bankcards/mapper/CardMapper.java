package com.bankcards.mapper;

import com.bankcards.dto.CardDto;
import com.bankcards.entity.Card;
import com.bankcards.entity.User;
import com.bankcards.util.CardMaskingService;
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
