package com.github.tah10n.carrentalbot.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class MessagesUtil {

    private MessagesUtil() {
    }

    public static String getMessage(String key, String lang) {
        ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale(lang));

        if (!messages.containsKey(key) || messages.getString(key).equals("null") || messages.getString(key).isEmpty()) {
            return key;
        }

        return messages.getString(key);
    }

    public static String getDatesAndPriceText(List<LocalDate> dates, String lang, int price) {
        if (dates == null || dates.isEmpty()) {
            return getMessage("dates_not_selected", lang);
        }
        StringBuilder result = new StringBuilder(getMessage("dates_selected", lang));

        String datesString = getDates(dates, lang);

        result.append(datesString);
        result.append(".\n");

        int totalPrice = calculateTotalPrice(dates, price);
        result.append(getMessage("total_price", lang)).append(totalPrice);

        return result.toString();
    }

    public static String getDates(List<LocalDate> dates, String lang) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        List<LocalDate> sortedDates = new ArrayList<>(dates);
        Collections.sort(sortedDates);


        LocalDate rangeStart = sortedDates.get(0);
        LocalDate rangeEnd = rangeStart;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);
            if (currentDate.isAfter(rangeEnd.plusDays(1))) {
                result.append(formatRange(rangeStart, rangeEnd, formatter, lang)).append(", ");
                rangeStart = currentDate;
            }
            rangeEnd = currentDate;
        }

        result.append(formatRange(rangeStart, rangeEnd, formatter, lang));
        return result.toString();
    }

    public static int calculateTotalPrice(List<LocalDate> dates, int price) {
        if (dates == null || dates.isEmpty()) {
            return 0;
        }
        return price * dates.size();
    }

    private static String formatRange(LocalDate start, LocalDate end, DateTimeFormatter formatter, String lang) {
        if (start.equals(end)) {
            return start.format(formatter);
        } else {
            return String.format(getMessage("from_to_dates", lang), start.format(formatter), end.format(formatter));
        }
    }
}
