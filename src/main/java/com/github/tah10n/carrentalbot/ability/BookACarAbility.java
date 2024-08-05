package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class BookACarAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;
    private final MessagesUtil messagesUtil;

    public BookACarAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService, MessagesUtil messagesUtil) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
        this.messagesUtil = messagesUtil;
    }

    public ReplyFlow chooseACarFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    String carId = upd.getCallbackQuery().getData().split(":")[1];
                    MyUser myUser = myUserDAO.getById(myUserId);
                    String lang = myUser.getLanguage();
                    LocalDate date = LocalDate.now();
                    String text = messagesUtil.getMessage("select_dates", lang) + " " + carService.getCarName(carId);
                    if (carService.hasCarPhoto(carId)) {
                        EditMessageCaption message = EditMessageCaption.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .caption(text)
                                .replyMarkup(keyboardMaker.getCalendarKeyboard(lang, carId, date, myUserId))
                                .build();
                        try {
                            bot.getTelegramClient().execute(message);
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    } else {
                        EditMessageText message = EditMessageText.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .text(text)
                                .replyMarkup(keyboardMaker.getCalendarKeyboard(lang, carId, date, myUserId))
                                .build();

                        try {
                            bot.getTelegramClient().execute(message);
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    }

                })
                .onlyIf(hasCallbackQueryWith("choose_car"))
                .build();
    }

    public ReplyFlow changeMonthFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    String changeMonth = split[1];
                    String language = split[2];
                    String carId = split[3];
                    LocalDate currentDate = LocalDate.parse(split[4]);
                    EditMessageReplyMarkup messageReplyMarkup = EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build();
                    if (changeMonth.equals("prev")) {
                        currentDate = currentDate.minusMonths(1);
                        if (currentDate.isBefore(LocalDate.now().minusMonths(1))) {
                            AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                                    .callbackQueryId(upd.getCallbackQuery().getId())
                                    .text(messagesUtil.getMessage("select_dates", language))
                                    .build();
                            bot.getSilent().execute(answerCallbackQuery);
                            return;
                        }
                        messageReplyMarkup.setReplyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId));
                    } else {
                        currentDate = currentDate.plusMonths(1);
                        if (currentDate.isAfter(LocalDate.now().plusMonths(4))) {
                            AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                                    .callbackQueryId(upd.getCallbackQuery().getId())
                                    .text(messagesUtil.getMessage("select_dates", language))
                                    .build();
                            bot.getSilent().execute(answerCallbackQuery);
                            return;
                        }
                        messageReplyMarkup.setReplyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId));
                    }

                    bot.getSilent().execute(messageReplyMarkup);
                })
                .onlyIf(hasCallbackQueryWith("change_month"))
                .build();
    }

    public ReplyFlow chooseDayFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    LocalDate date = LocalDate.parse(split[1]);
                    String language = split[2];
                    String carId = split[3];
                    LocalDate currentDate = LocalDate.parse(split[4]);
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    Car car = carService.addDate(myUserId, carId, date);
                    String text = messagesUtil.getMessage("select_dates", language) +
                            car.toString() +
                            ". " +
                            formatDateRangeText(carService.getBookedDates(myUserId, carId), language);

                    if (carService.hasCarPhoto(carId)) {
                        EditMessageCaption message = EditMessageCaption.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .caption(text)
                                .replyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId))
                                .build();
                        try {
                            bot.getTelegramClient().execute(message);
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    } else {
                        EditMessageText message = EditMessageText.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .text(text)
                                .replyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId))
                                .build();

                        try {
                            bot.getTelegramClient().execute(message);
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    }
                })
                .onlyIf(hasCallbackQueryWith("choose_date"))
                .build();
    }

    public ReplyFlow cancelFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    String language = split[1];
                    String carId = split[2];
                    carService.clearDates(myUserId, carId);
                    String text = carService.getCarName(carId);
                    if (carService.hasCarPhoto(carId)) {
                        EditMessageCaption message = EditMessageCaption.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .caption(text)
                                .replyMarkup(keyboardMaker.getChooseCarKeyboard(carId, language))
                                .build();
                        try {
                            bot.getTelegramClient().execute(message);
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    } else {
                        EditMessageText message = EditMessageText.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .text(text)
                                .replyMarkup(keyboardMaker.getChooseCarKeyboard(carId, language))
                                .build();

                        try {
                            bot.getTelegramClient().execute(message);
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    }

                })
                .onlyIf(hasCallbackQueryWith("cancel_booking"))
                .build();
    }

    public ReplyFlow bookACar() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    String language = split[1];
                    String carId = split[2];
                    try {
                        carService.bookACar(myUserId, carId);
                    } catch (NullPointerException | IllegalArgumentException e) {
                        log.error(e.getMessage(), e);
                        carService.clearDates(myUserId, carId);
                        String text = messagesUtil.getMessage("select_dates", language) + " " + carService.getCarName(carId);
                        EditMessageText message = EditMessageText.builder()
                                .chatId(chatId)
                                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                                .text(text)
                                .replyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, LocalDate.now(), myUserId))
                                .build();
                        bot.getSilent().execute(message);

                        bot.getSilent().send(messagesUtil.getMessage("car_not_booked", language), myUserId);
                        return;
                    }
                    carService.clearDates(myUserId, carId);
                    EditMessageText message = EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                            .text(messagesUtil.getMessage("car_booked", language) + " " + carService.getCarName(carId))
                            .replyMarkup(keyboardMaker.getBackKeyboard(language))
                            .build();
                    bot.getSilent().execute(message);
                })
                .onlyIf(hasCallbackQueryWith("book_car"))
                .build();
    }

    private String formatDateRangeText(List<LocalDate> dates, String lang) {
        if (dates == null || dates.isEmpty()) {
            return messagesUtil.getMessage("dates_not_selected", lang);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        List<LocalDate> sortedDates = new ArrayList<>(dates);
        Collections.sort(sortedDates);

        StringBuilder result = new StringBuilder(messagesUtil.getMessage("dates_selected", lang));
        LocalDate rangeStart = sortedDates.get(0);
        LocalDate rangeEnd = rangeStart;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);
            if (currentDate.isAfter(rangeEnd.plusDays(1))) {
                // Если есть промежуток, добавляем текущий диапазон и начинаем новый
                result.append(formatRange(rangeStart, rangeEnd, formatter)).append(", ");
                rangeStart = currentDate;
            }
            rangeEnd = currentDate;
        }

        // Добавляем последний диапазон
        result.append(formatRange(rangeStart, rangeEnd, formatter));

        return result.toString();
    }

    public ReplyFlow ignore() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(myUserId);
                    String lang = myUser.getLanguage();
                    AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                            .callbackQueryId(upd.getCallbackQuery().getId())
                            .text(messagesUtil.getMessage("select_dates", lang))
                            .build();
                    bot.getSilent().execute(answerCallbackQuery);
                })
                .onlyIf(hasCallbackQueryWith("ignore_calendar"))
                .build();
    }

    private String formatRange(LocalDate start, LocalDate end, DateTimeFormatter formatter) {
        if (start.equals(end)) {
            return start.format(formatter);
        } else {
            return start.format(formatter) + " - " + end.format(formatter);
        }
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() && update.getCallbackQuery().getData().contains(string);
    }
}
