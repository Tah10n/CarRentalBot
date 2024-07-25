package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.CarDAO;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class RentACarAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarDAO carDAO;
    private final MessagesUtil messagesUtil;

    public RentACarAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarDAO carDAO, MessagesUtil messagesUtil) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carDAO = carDAO;
        this.messagesUtil = messagesUtil;
    }

    public ReplyFlow rentACarFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    String carId = upd.getCallbackQuery().getData().split(":")[1];
                    Car car = carDAO.getById(carId);
                    MyUser myUser = myUserDAO.getById(myUserId);
                    String lang = myUser.getLanguage();
                    LocalDate date = LocalDate.now();
                    String text = messagesUtil.getMessage("select_dates", lang) + " " + car.toString();
                    EditMessageText message = EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                            .text(text)
                            .replyMarkup(keyboardMaker.getCalendarKeyboard(lang, carId, date, myUserId))
                            .build();

                    try {
                        bot.getTelegramClient().execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
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
                        messageReplyMarkup.setReplyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId));
                    } else {
                        currentDate = currentDate.plusMonths(1);
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
                    Car car = carDAO.getById(carId);
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    Map<Long, List<LocalDate>> map = car.getMap();
                    if(map==null) {
                        map = new HashMap<>();
                    }
                    if(!map.containsKey(myUserId)) {
                        map.put(myUserId, List.of(date));
                    } else {
                        List<LocalDate> bookedDates = map.get(myUserId);
                        if(bookedDates.contains(date)) {
                            bookedDates.remove(date);
                        } else {
                            bookedDates.add(date);
                        }
                    }
                    car.setMap(map);
                    carDAO.save(car);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(messagesUtil.getMessage("select_dates", language));
                    stringBuilder.append(car.toString());
                    stringBuilder.append(". ");
                    stringBuilder.append(formatDateRangeText(car.getBookedDates(myUserId), language));
                    String text = stringBuilder.toString();

                    EditMessageText messageText = EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(text)
                            .replyMarkup(keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId))
                            .build();

                    bot.getSilent().execute(messageText);
                })
                .onlyIf(hasCallbackQueryWith("choose_date"))
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
