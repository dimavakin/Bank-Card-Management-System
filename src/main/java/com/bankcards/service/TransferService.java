package com.bankcards.service;

import com.bankcards.dto.TransferRequest;
import com.bankcards.dto.TransferResponse;

public interface TransferService {
    TransferResponse transferBetweenUserCards(TransferRequest request, String email);
}
