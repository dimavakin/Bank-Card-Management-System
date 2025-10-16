package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class CardGenerator {
    private static final Random random = new Random();

    public Card generateCard(User user) {
        Card card = new Card();
        card.setCardNumber(generateCardNumber());
        card.setExpiryDate(generateExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);
        card.setCreatedAt(LocalDateTime.now());

        return card;
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }

        return cardNumber.toString();
    }

    private LocalDate generateExpiryDate() {
        return LocalDate.now().plusYears(3);
    }
}
