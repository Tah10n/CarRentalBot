package com.github.tah10n.carrentalbot.keyboards;

import com.github.tah10n.carrentalbot.service.CarService;
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

import static com.github.tah10n.carrentalbot.utils.ButtonsUtil.getButton;

@Component
public class InlineKeyboardMaker {
    private final CarService carService;

    public InlineKeyboardMaker(CarService carService) {
        this.carService = carService;
    }

    public InlineKeyboardMarkup getStartKeyboard(String lang, Long myUserId) {
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(InlineKeyboardButton.builder().text(getButton("list_of_cars", lang)).callbackData("list_of_cars").build());
        if (carService.isUserHasBookings(myUserId)) {
            InlineKeyboardButton bookingsButton = InlineKeyboardButton.builder().text(getButton("my_bookings", lang))
                    .callbackData("my_bookings").build();
            row.add(bookingsButton);
        }

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(InlineKeyboardButton.builder().text(getButton("help", lang)).callbackData("help").build());
        row2.add(InlineKeyboardButton.builder().text(getButton("rules", lang)).callbackData("rules").build());
        row2.add(InlineKeyboardButton.builder().text(getButton("contacts", lang)).callbackData("contacts").build());

        keyboardMarkup.setKeyboard(List.of(row, row2));
        return keyboardMarkup;
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
                        InlineKeyboardButton.builder().text(getButton("back", lang)).callbackData("back").build()
                )).build();
    }

    public InlineKeyboardMarkup getChooseCarKeyboard(String carId, String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(getButton("back", lang)).callbackData("back").build(),
                        InlineKeyboardButton.builder().text(getButton("choose_car", lang)).callbackData("choose_car:" + carId).build()
                        )).build();
    }

    public InlineKeyboardMarkup getAddCarKeyboard(String language) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(getButton("add_car", language)).callbackData("add_car").build()
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

            List<LocalDate> chosenDates = carService.getChosenDates(myUserId, carId);

            String callbackData = ignoreCallbackData;
            if (date.isBefore(LocalDate.now().plusDays(1))) {
                buttonText = buttonText + " ❌";
            } else if (carService.isBookedDate(date, carId)) {
                buttonText = buttonText + " ❌";
            } else {
                if (chosenDates != null && chosenDates.contains(date)) {
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

                row.add(InlineKeyboardButton.builder().text(" ").callbackData(ignoreCallbackData).build());
            }
            keyboardRows.add(new InlineKeyboardRow(row));
        }

        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        navigationRow.add(InlineKeyboardButton.builder().text("◀️").callbackData("change_month:prev:" + language + ":" + carId + ":" + currentDate).build());
        navigationRow.add(InlineKeyboardButton.builder().text("▶️").callbackData("change_month:next:" + language + ":" + carId + ":" + currentDate).build());

        keyboardRows.add(new InlineKeyboardRow(navigationRow));

        keyboardRows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text(getButton("cancel_booking", language)).callbackData("cancel_booking:" + language + ":" + carId).build(),
                InlineKeyboardButton.builder().text(getButton("book_car", language)).callbackData("book_car:" + language + ":" + carId).build()
        ));

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();
    }

    public InlineKeyboardMarkup getDeleteBookingKeyboard(String bookId, String lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(getButton("back", lang)).callbackData("back").build(),
                        InlineKeyboardButton.builder().text(getButton("delete_booking", lang)).callbackData("delete_booking:" + bookId).build()
                ))
                .build();
    }
}
