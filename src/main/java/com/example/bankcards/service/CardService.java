package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public CardService(CardRepository cardRepository, UserRepository userRepository, EncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    @Transactional
    public Card createCard(Card card) {
        // Шифруем номер карты перед сохранением
        if (card.getCardNumber() != null) {
            card.setCardNumber(encryptionService.encrypt(card.getCardNumber()));
        }
        return cardRepository.save(card);
    }

    @Transactional
    public Card updateCard(Long id, Card updatedCard) {
        return cardRepository.findById(id).map(card -> {
            if (updatedCard.getCardNumber() != null) {
                card.setCardNumber(encryptionService.encrypt(updatedCard.getCardNumber()));
            }
            card.setOwner(updatedCard.getOwner());
            card.setExpirationDate(updatedCard.getExpirationDate());
            card.setStatus(updatedCard.getStatus());
            card.setBalance(updatedCard.getBalance());
            return cardRepository.save(card);
        }).orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    @Transactional
    public void transferBetweenCards(Long fromCardId, Long toCardId, Double amount) {
        Card fromCard = cardRepository.findById(fromCardId)
            .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card toCard = cardRepository.findById(toCardId)
            .orElseThrow(() -> new RuntimeException("Destination card not found"));

        if (fromCard.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Source card is not active");
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Destination card is not active");
        }

        fromCard.setBalance(fromCard.getBalance() - amount);
        toCard.setBalance(toCard.getBalance() + amount);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Transactional
    public void requestCardBlock(Long cardId) {
        Card card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public List<Card> filterCards(String owner, CardStatus status, int page, int size, String currentUsername) {
        Pageable pageable = PageRequest.of(page, size);

        // Проверяем роль пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // ADMIN видит все карты
            return cardRepository.findByOwnerAndStatus(owner, status, pageable);
        } else {
            // USER видит только свои карты
            User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
            return cardRepository.findByUserAndStatus(user, status, pageable);
        }
    }

    public List<Card> getUserCards(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findByUser(user, pageable);
    }

    // Методы для проверки прав доступа (используются в @PreAuthorize)
    public boolean isCardOwner(Long cardId, String username) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            return false;
        }

        User user = userRepository.findByUsername(username)
            .orElse(null);

        return user != null && cardOpt.get().getUser().getId().equals(user.getId());
    }

    public boolean areCardsOwnedByUser(Long fromCardId, Long toCardId, String username) {
        return isCardOwner(fromCardId, username) && isCardOwner(toCardId, username);
    }
}
