package com.smartbill.util;

import java.math.BigDecimal;

public class NumberToWordsConverter {

    private static final String[] ONES = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(BigDecimal amount) {
        long number = amount.longValue();
        if (number == 0) return "Zero Rupees";

        String words = convertToIndianWords(number);
        return words.trim() + " Rupees";
    }

    private static String convertToIndianWords(long number) {
        if (number == 0) return "";

        StringBuilder result = new StringBuilder();

        // Crores (1,00,00,000+)
        if (number >= 10000000) {
            result.append(convertToIndianWords(number / 10000000)).append(" Crore ");
            number %= 10000000;
        }

        // Lakhs (1,00,000+)
        if (number >= 100000) {
            result.append(convertToIndianWords(number / 100000)).append(" Lakh ");
            number %= 100000;
        }

        // Thousands (1,000+)
        if (number >= 1000) {
            result.append(convertToIndianWords(number / 1000)).append(" Thousand ");
            number %= 1000;
        }

        // Hundreds (100+)
        if (number >= 100) {
            result.append(ONES[(int) (number / 100)]).append(" Hundred ");
            number %= 100;
        }

        // Tens and Ones
        if (number >= 20) {
            result.append(TENS[(int) (number / 10)]).append(" ");
            number %= 10;
        }

        if (number > 0) {
            result.append(ONES[(int) number]).append(" ");
        }

        return result.toString().trim();
    }
}
