package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import com.github.tah10n.carrentalbot.service.GoogleSheetsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.editMessage;
import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.executeMessage;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getDatesAndPriceText;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
@Component
public class BookACarAbility implements AbilityExtension {
    private final BaseAbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;
    private final GoogleSheetsService googleSheetsService;

    public BookACarAbility(BaseAbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService, GoogleSheetsService googleSheetsService) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
        this.googleSheetsService = googleSheetsService;
    }



    public ReplyFlow chooseACarFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    Long chatId = upd.getCallbackQuery().getFrom().getId();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String carId = upd.getCallbackQuery().getData().split(":")[1];
                    MyUser myUser = myUserDAO.getById(myUserId);
                    String lang = myUser.getLanguage();
                    LocalDate date = LocalDate.now();
                    String text = getMessage("select_dates", lang) + " " + carService.getCarName(carId);
                    InlineKeyboardMarkup calendarKeyboard = keyboardMaker.getCalendarKeyboard(lang, carId, date, myUserId);
                    editMessage(bot, upd, chatId, messageId, text, calendarKeyboard);

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
                                    .text(getMessage("select_dates", language))
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
                                    .text(getMessage("select_dates", language))
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

    public ReplyFlow chooseDaysFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long chatId = upd.getCallbackQuery().getFrom().getId();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    LocalDate date = LocalDate.parse(split[1]);
                    String language = split[2];
                    String carId = split[3];
                    LocalDate currentDate = LocalDate.parse(split[4]);
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    carService.addDate(myUserId, carId, date);
                    List<LocalDate> chosenDates = carService.getChosenDates(myUserId, carId);
                    int pricePerDay = carService.getPricePerDay(carId);

                    if (carService.checkIsChosenDatesAlreadyOccupied(chosenDates, carId)) {
                        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                                .callbackQueryId(upd.getCallbackQuery().getId())
                                .text(getMessage("select_unoccupied_dates", language))
                                .build();
                        bot.getSilent().execute(answerCallbackQuery);
                        carService.deleteEndDate(myUserId, carId);
                    } else {
                        String text = getMessage("select_dates", language) +
                                carService.getCarName(carId) +
                                ". " +
                                getDatesAndPriceText(chosenDates, language, pricePerDay);
                        InlineKeyboardMarkup calendarKeyboard = keyboardMaker.getCalendarKeyboard(language, carId, currentDate, myUserId);
                        editMessage(bot, upd, chatId, messageId, text, calendarKeyboard);
                    }

                })
                .onlyIf(hasCallbackQueryWith("choose_date"))
                .build();
    }

    public ReplyFlow cancelFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    Long chatId = upd.getCallbackQuery().getFrom().getId();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    String language = split[1];
                    String carId = split[2];
                    carService.clearDates(myUserId, carId);
                    String text = carService.getCarById(carId).toString();
                    InlineKeyboardMarkup keyboard = keyboardMaker.getChooseCarKeyboard(carId, language);
                    editMessage(bot, upd, chatId, messageId, text, keyboard);

                })
                .onlyIf(hasCallbackQueryWith("cancel_booking"))
                .build();
    }

    public ReplyFlow bookACar() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    Long chatId = upd.getCallbackQuery().getFrom().getId();
                    Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    String language = split[1];
                    String carId = split[2];
                    BookingHistory bookingHistory = null;
                    try {
                        bookingHistory = carService.bookACar(myUserId, carId);
                        String bookingHistoryId = bookingHistory.getId();
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.submit(() -> {
                            try {
                                googleSheetsService.appendValues(bookingHistoryId);
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                            }
                        });
                        executor.shutdown();
                    } catch (NullPointerException | IllegalArgumentException e) {
                        log.error(e.getMessage(), e);
                        carService.clearDates(myUserId, carId);
                        String text = getMessage("select_dates", language) + " " + carService.getCarName(carId);
                        InlineKeyboardMarkup keyboard = keyboardMaker.getCalendarKeyboard(language, carId, LocalDate.now(), myUserId);
                        editMessage(bot, upd, chatId, messageId, text, keyboard);

                        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                                .callbackQueryId(upd.getCallbackQuery().getId())
                                .text(getMessage("car_not_booked", language))
                                .showAlert(true)
                                .build();
                        bot.getSilent().execute(answerCallbackQuery);
                        return;
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    if (bookingHistory == null) {
                        return;
                    }
                    List<LocalDate> bookedDates = bookingHistory.getBookedDates();
                    int pricePerDay = carService.getPricePerDay(bookingHistory.getCarId());
                    carService.clearDates(myUserId, carId);
                    String carBookedText = getMessage("car_booked", language) + " " + carService.getCarName(carId)
                            + "\n" + getDatesAndPriceText(bookedDates, language, pricePerDay);
                    InlineKeyboardMarkup backKeyboard = keyboardMaker.getBackKeyboard(language);

                    editMessage(bot, upd, chatId, messageId, carBookedText, backKeyboard);
                })
                .onlyIf(hasCallbackQueryWith("book_car"))
                .build();
    }


    public ReplyFlow ignore() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getCallbackQuery().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(myUserId);
                    String lang = myUser.getLanguage();
                    AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                            .callbackQueryId(upd.getCallbackQuery().getId())
                            .text(getMessage("select_dates", lang))
                            .build();
                    bot.getSilent().execute(answerCallbackQuery);
                })
                .onlyIf(hasCallbackQueryWith("ignore_calendar"))
                .build();
    }

    public ReplyFlow myBookingsFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action(this::sendListOfBookings)
                .onlyIf(hasCallbackQueryWith("my_bookings"))
                .build();
    }

    public ReplyFlow deleteBookingFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    String[] split = upd.getCallbackQuery().getData().split(":");
                    String bookId = split[1];
                    DeleteMessage deleteMessage = DeleteMessage.builder()
                            .chatId(getChatId(upd))
                            .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                            .build();

                    carService.deleteBookingById(bookId);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(() -> {
                        try {
                            googleSheetsService.deleteValues(bookId);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                    executor.shutdown();

                    bot.getSilent().execute(deleteMessage);
                })
                .onlyIf(hasCallbackQueryWith("delete_booking"))
                .build();
    }

    private void sendListOfBookings(BaseAbilityBot bot, Update upd) {
        MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
        String lang = myUser.getLanguage();
        List<BookingHistory> bookingsByUserId = carService.getBookingsByUserId(myUser.getId());
        if (bookingsByUserId.isEmpty()) {
            String text = getMessage("no_bookings", lang);
            AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                    .callbackQueryId(upd.getCallbackQuery().getId())
                    .text(text)
                    .build();
            bot.getSilent().execute(answerCallbackQuery);
            return;
        }

        for (BookingHistory book : bookingsByUserId) {
            List<LocalDate> bookedDates = book.getBookedDates();
            int pricePerDay = carService.getPricePerDay(book.getCarId());
            String bookedDatesString = getDatesAndPriceText(bookedDates, lang, pricePerDay);
            String text = carService.getCarName(book.getCarId()) + "\n" + bookedDatesString;
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(getChatId(upd))
                    .text(text)
                    .replyMarkup(keyboardMaker.getDeleteBookingKeyboard(book.getId(), lang))
                    .build();

            executeMessage(bot, myUser.getId(), myUserDAO, sendMessage);
        }
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() && update.getCallbackQuery().getData().contains(string);
    }
}
