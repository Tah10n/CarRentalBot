package com.github.tah10n.carrentalbot.utils;

import org.springframework.stereotype.Component;

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

    public static String formatDateRangeText(List<LocalDate> dates, String lang) {
        if (dates == null || dates.isEmpty()) {
            return getMessage("dates_not_selected", lang);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        List<LocalDate> sortedDates = new ArrayList<>(dates);
        Collections.sort(sortedDates);

        StringBuilder result = new StringBuilder(getMessage("dates_selected", lang));
        LocalDate rangeStart = sortedDates.get(0);
        LocalDate rangeEnd = rangeStart;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);
            if (currentDate.isAfter(rangeEnd.plusDays(1))) {
                // Если есть промежуток, добавляем текущий диапазон и начинаем новый
                result.append(formatRange(rangeStart, rangeEnd, formatter, lang)).append(", ");
                rangeStart = currentDate;
            }
            rangeEnd = currentDate;
        }

        // Добавляем последний диапазон
        result.append(formatRange(rangeStart, rangeEnd, formatter, lang));

        return result.toString();
    }

    private static String formatRange(LocalDate start, LocalDate end, DateTimeFormatter formatter, String lang) {
        if (start.equals(end)) {
            return start.format(formatter);
        } else {
            return String.format(getMessage("from_to_dates", lang), start.format(formatter), end.format(formatter));
        }
    }
}
