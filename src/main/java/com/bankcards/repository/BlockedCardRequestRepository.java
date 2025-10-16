package com.bankcards.repository;

import com.bankcards.entity.BlockCardRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedCardRequestRepository extends JpaRepository<BlockCardRequest, Long> {
}
