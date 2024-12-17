package com.dogood.dogoodbackend.utils;

public class ValidateFields {
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if(phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        phoneNumber = phoneNumber.replaceAll(" ", "");
        return phoneNumber.matches("^(\\+972|0)5\\d-?\\d{7}$");
    }

    public static boolean isValidEmail(String email) {
        if(email == null || email.isBlank()) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidText(String text, int min, int max) {
        return text != null &&
                !text.isBlank() &&
                text.length() >= min &&
                text.length() <= max;
    }

}
