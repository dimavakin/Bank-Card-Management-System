package com.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskingService {
    public String maskCardNumber(String cardNumber) {
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
