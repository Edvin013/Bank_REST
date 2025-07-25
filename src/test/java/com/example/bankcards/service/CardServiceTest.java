package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFilterCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Card card1 = new Card();
        card1.setOwner("John Doe");
        card1.setStatus(CardStatus.ACTIVE);

        Card card2 = new Card();
        card2.setOwner("Jane Doe");
        card2.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findByOwnerAndStatus("John Doe", CardStatus.ACTIVE, pageable))
                .thenReturn(Arrays.asList(card1));

        List<Card> result = cardService.filterCards("John Doe", CardStatus.ACTIVE, 0, 10);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getOwner());
    }

    @Test
    void testTransferBetweenCards() {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(100.0);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(50.0);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        cardService.transferBetweenCards(1L, 2L, 30.0);

        assertEquals(70.0, fromCard.getBalance());
        assertEquals(80.0, toCard.getBalance());

        verify(cardRepository, times(1)).save(fromCard);
        verify(cardRepository, times(1)).save(toCard);
    }

    @Test
    void testRequestCardBlock() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.requestCardBlock(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testMaskCardNumber() {
        String cardNumber = "1234567812345678";
        String maskedCard = CardUtils.maskCardNumber(cardNumber);
        assertEquals("**** **** **** 5678", maskedCard);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CardUtils.maskCardNumber("123");
        });
        assertEquals("Некорректный номер карты", exception.getMessage());
    }
}
