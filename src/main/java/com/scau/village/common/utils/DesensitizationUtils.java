package com.scau.village.common.utils;

public class DesensitizationUtils {
    public static String mobile(String mobile) {
        if (mobile == null || mobile.length() < 11) return mobile;
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 18) return idCard;
        return idCard.replaceAll("(\\d{4})\\d{10}(\\d{4})", "$1**********$2");
    }
}