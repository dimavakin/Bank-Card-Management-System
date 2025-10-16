package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUser(User user, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE " +
            "(:status IS NULL OR c.status = :status)")
    Page<Card> findAllWithFilters(@Param("status") CardStatus status,
                                  Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.email = :email AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<Card> findByUserWithFilters(@Param("email") String email,
                                     @Param("status") CardStatus status,
                                     Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.email = :email AND " +
            "c.id = :cardId")
    Optional<Card> findByIdAndEmail(@Param("cardId")Long id,@Param("email")  String email);

    @Query("SELECT SUM(c.balance) FROM Card c WHERE c.user.email = :email")
    Optional<BigDecimal> getBalanceByEmail(@Param("email") String email);
}
