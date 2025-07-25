package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    // Поиск карт по владельцу и статусу с пагинацией
    List<Card> findByOwnerAndStatus(String owner, CardStatus status, Pageable pageable);

    // Поиск карт по пользователю
    List<Card> findByUser(User user, Pageable pageable);

    // Поиск карт по пользователю и статусу
    List<Card> findByUserAndStatus(User user, CardStatus status, Pageable pageable);

    // Поиск всех карт пользователя
    List<Card> findByUser(User user);
}
