package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber; // Хранится в зашифрованном виде

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(nullable = false)
    private Double balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Связь с пользователем


    public Card() {}

    public Card(String cardNumber, String owner, LocalDate expirationDate, CardStatus status, Double balance, User user) {
        this.cardNumber = cardNumber;
        this.owner = owner;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.user = user;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает маскированный номер карты для отображения
     */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        return "**** **** **** " + getLastFourDigits();
    }

    /**
     * Получает последние 4 цифры номера карты (для маскирования)
     * В реальном приложении это должно работать с зашифрованными данными
     */
    private String getLastFourDigits() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return "****";
    }
}
