package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockCardRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedCardRequestRepository extends JpaRepository<BlockCardRequest, Long> {
}
