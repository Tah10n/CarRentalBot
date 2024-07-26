package com.github.tah10n.carrentalbot.keyboards;

import com.github.tah10n.carrentalbot.service.CarService;
import com.github.tah10n.carrentalbot.utils.ButtonsUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class InlineKeyboardMaker {
    private final ButtonsUtil buttonsUtil;
    private final CarService carService;

    public InlineKeyboardMaker(ButtonsUtil buttonsUtil, CarService carService) {
        this.buttonsUtil = buttonsUtil;
        this.carService = carService;
    }

    public InlineKeyboardMarkup getStartKeyboard(String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("list_of_cars", lang)).callbackData("list_of_cars").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("help", lang)).callbackData("help").build(),
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("rules", lang)).callbackData("rules").build(),
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("contacts", lang)).callbackData("contacts").build()
                )).build();
    }

    public InlineKeyboardMarkup getLanguageKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("RU").callbackData("lang_ru").build(),
                        InlineKeyboardButton.builder().text("EN").callbackData("lang_en").build(),
                        InlineKeyboardButton.builder().text("SR").callbackData("lang_sr").build()
                )).build();
    }

    public InlineKeyboardMarkup getBackKeyboard(String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("back", lang)).callbackData("back").build()
                )).build();
    }

    public InlineKeyboardMarkup getChooseCarKeyboard(String carId, String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("choose_car", lang)).callbackData("choose_car:" + carId).build()
                )).build();
    }

    public InlineKeyboardMarkup getAddCarKeyboard(String language) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(buttonsUtil.getButton("add_car", language)).callbackData("add_car").build()
                )).build();
    }

    public InlineKeyboardMarkup getCalendarKeyboard(String language, String carId, LocalDate currentDate, Long myUserId) {

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        String ignoreCallbackData = "ignore_calendar";
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        String monthDisplayName = yearMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag(language));
        List<InlineKeyboardButton> header = new ArrayList<>();
        header.add(InlineKeyboardButton.builder().text(monthDisplayName + " " + yearMonth.getYear()).callbackData(ignoreCallbackData).build());
        keyboardRows.add(new InlineKeyboardRow(header));

        List<InlineKeyboardButton> daysOfWeek = new ArrayList<>();

        String[] weekDays = {
                DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language)),
                DayOfWeek.TUESDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language)),
                DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language)),
                DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language)),
                DayOfWeek.FRIDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language)),
                DayOfWeek.SATURDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language)),
                DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag(language))
        };
        for (String day : weekDays) {
            daysOfWeek.add(InlineKeyboardButton.builder().text(day).callbackData(ignoreCallbackData).build());
        }
        keyboardRows.add(new InlineKeyboardRow(daysOfWeek));

        List<InlineKeyboardButton> row = new ArrayList<>();
        int monthLength = yearMonth.lengthOfMonth();
        for (int i = 1; i < dayOfWeek; i++) {
            row.add(InlineKeyboardButton.builder().text(" ").callbackData(ignoreCallbackData).build());
        }
        for (int day = 1; day <= monthLength; day++) {
            String buttonText = String.valueOf(day);
            LocalDate date = yearMonth.atDay(day);

            List<LocalDate> bookedDates = carService.getBookedDates(myUserId, carId);

            String callbackData = ignoreCallbackData;
            if (date.isBefore(LocalDate.now())) {
                buttonText = buttonText + " ❌";
            } else {

                if (bookedDates != null && bookedDates.contains(date)) {
                    buttonText = buttonText + " ✅";

                }
                callbackData = "choose_date:" + date + ":" + language + ":" + carId + ":" + currentDate;
            }

            row.add(InlineKeyboardButton.builder().text(buttonText).callbackData(callbackData).build());
            if (row.size() == 7) {
                keyboardRows.add(new InlineKeyboardRow(row));
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {

            while (row.size() < 7) {

                row.add(InlineKeyboardButton.builder().text(" ").callbackData("ignore").build());
            }
            keyboardRows.add(new InlineKeyboardRow(row));
        }

        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        navigationRow.add(InlineKeyboardButton.builder().text("◀️").callbackData("change_month:prev:" + language + ":" + carId + ":" + currentDate).build());
        navigationRow.add(InlineKeyboardButton.builder().text("▶️").callbackData("change_month:next:" + language + ":" + carId + ":" + currentDate).build());

        keyboardRows.add(new InlineKeyboardRow(navigationRow));


        return InlineKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();
    }
}
