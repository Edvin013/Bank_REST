package com.example.bankcards.util;

public class CardUtils {

    /**
     * Маскирует номер карты, оставляя только последние 4 цифры.
     * @param cardNumber Полный номер карты.
     * @return Маскированный номер карты.
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Некорректный номер карты");
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
