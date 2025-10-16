package com.bankcards.service.impl;

import com.bankcards.dto.TransferRequest;
import com.bankcards.dto.TransferResponse;
import com.bankcards.exception.CardNotActiveException;
import com.bankcards.exception.InsufficientFundsException;
import com.bankcards.repository.CardRepository;
import com.bankcards.service.TransferService;
import com.bankcards.entity.Card;
import com.bankcards.entity.CardStatus;
import com.bankcards.exception.InvalidTransferAmountException;
import com.bankcards.exception.NotFoundException;
import com.bankcards.exception.SameCardTransferException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferServiceImpl implements TransferService {
    CardRepository cardRepository;

    @Override
    @Transactional
    public TransferResponse transferBetweenUserCards(TransferRequest request, String email) {
        Card sourceCard = cardRepository.findByIdAndEmail(request.getSourceCardId(), email)
                .orElseThrow(() -> new NotFoundException(String.format("Source card not found: %s", request.getSourceCardId())));

        Card targetCard = cardRepository.findByIdAndEmail(request.getTargetCardId(), email)
                .orElseThrow(() -> new NotFoundException(String.format("Target card not found: %s", request.getTargetCardId())));

        if (sourceCard.getStatus() != CardStatus.ACTIVE
                || targetCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Both cards must be active");
        }
        if (request.getAmount().compareTo(sourceCard.getBalance()) > 0) {
            throw new InsufficientFundsException(sourceCard.getBalance(), request.getAmount());
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferAmountException("Transfer amount must be positive");
        }
        if (sourceCard.getId().equals(targetCard.getId())) {
            throw new SameCardTransferException("Source and target cards must be different");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(request.getAmount()));
        targetCard.setBalance(targetCard.getBalance().add(request.getAmount()));

        cardRepository.save(sourceCard);
        cardRepository.save(targetCard);

        return new TransferResponse(
                sourceCard.getId(),
                targetCard.getId(),
                request.getAmount(),
                LocalDateTime.now()
        );
    }
}
