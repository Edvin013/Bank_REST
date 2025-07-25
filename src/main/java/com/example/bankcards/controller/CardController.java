package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @cardService.isCardOwner(#id, authentication.name)")
    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
        Optional<Card> card = cardService.getCardById(id);
        return card.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Только ADMIN может создавать карты
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Card createCard(@RequestBody Card card) {
        return cardService.createCard(card);
    }

    // Только ADMIN может обновлять карты
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Card> updateCard(@PathVariable Long id, @RequestBody Card updatedCard) {
        try {
            Card card = cardService.updateCard(id, updatedCard);
            return ResponseEntity.ok(card);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') and @cardService.areCardsOwnedByUser(#fromCardId, #toCardId, authentication.name)")
    public ResponseEntity<String> transferBetweenCards(@RequestParam Long fromCardId, @RequestParam Long toCardId, @RequestParam Double amount) {
        try {
            cardService.transferBetweenCards(fromCardId, toCardId, amount);
            return ResponseEntity.ok("Transfer successful");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // USER может блокировать только свои карты
    @PostMapping("/block")
    @PreAuthorize("hasRole('USER') and @cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<String> requestCardBlock(@RequestParam Long cardId) {
        try {
            cardService.requestCardBlock(cardId);
            return ResponseEntity.ok("Card blocked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/filter")
    public ResponseEntity<List<Card>> filterCards(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        List<Card> filteredCards = cardService.filterCards(owner, status, page, size, principal.getName());
        return ResponseEntity.ok(filteredCards);
    }

    // USER может видеть только свои карты
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Card>> getMyCards(Principal principal,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        List<Card> myCards = cardService.getUserCards(principal.getName(), page, size);
        return ResponseEntity.ok(myCards);
    }
}
