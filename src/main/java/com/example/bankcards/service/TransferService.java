package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.User;

public interface TransferService {
    TransferResponse transferBetweenUserCards(TransferRequest request, String email);
}
