package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.editMessage;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getDateAndPriceText;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
public class StartAbility implements AbilityExtension {
    private final BaseAbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;

    public StartAbility(BaseAbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
    }

    public Ability startCommand() {
        return Ability
                .builder()
                .name("start")
                .info("starts the bot")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    User user = ctx.user();
                    String lang;
                    MyUser myUser;
                    if (!myUserDAO.existsById(user.getId())) {
                        myUser = new MyUser(user.getId(), user.getFirstName(), user.getLastName(), user.getUserName(),
                                false
                                , false, user.getLanguageCode(), new Stack<>());
                        myUserDAO.save(myUser);
                        String greeting = getMessage("greeting", myUser.getLanguage());
                        abilityBot.getSilent().send(greeting, ctx.chatId());
                    } else {
                        myUser = myUserDAO.getById(user.getId());
                        myUser.setIsUnsubscribed(false);
                        myUserDAO.save(myUser);
                    }
                    lang = myUser.getLanguage();


                    String text = getMessage("start", lang);
                    SendMessage message = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .replyMarkup(keyboardMaker.getStartKeyboard(lang, myUser.getId())).build();
                    abilityBot.getSilent().execute(message);

                }).build();
    }

    public ReplyFlow helpButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> simpleButtonAction(upd, "help", bot))
                .onlyIf(hasCallbackQueryWith("help"))
                .build();
    }

    public ReplyFlow rulesButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> simpleButtonAction(upd, "rules", bot))
                .onlyIf(hasCallbackQueryWith("rules"))
                .build();
    }

    public ReplyFlow contactsButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> simpleButtonAction(upd, "contacts", bot))
                .onlyIf(hasCallbackQueryWith("contacts"))
                .build();
    }

    private void simpleButtonAction(Update upd, String actionName, BaseAbilityBot bot) {
        MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
        Long chatId = upd.getCallbackQuery().getFrom().getId();
        Integer messageId = upd.getCallbackQuery().getMessage().getMessageId();
        String lang = myUser.getLanguage();
        String text = getMessage(actionName, lang);
        InlineKeyboardMarkup backKeyboard = keyboardMaker.getBackKeyboard(lang);

        Message message = editMessage(bot, upd, chatId, messageId, text, backKeyboard);
        if (message == null) {
            return;
        }
        myUserDAO.addMessageToStack(myUser.getId(), message.getMessageId());
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
                    bot.getSilent().execute(deleteMessage);
                })
                .onlyIf(hasCallbackQueryWith("delete_booking"))
                .build();
    }

    public ReplyFlow unsubscribeFlow() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long myUserId = upd.getMyChatMember().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(myUserId);
                    myUser.setIsUnsubscribed(true);
                    myUserDAO.save(myUser);

                })
                .onlyIf(hasStatusKicked())
                .build();
    }

    private Predicate<Update> hasStatusKicked() {
        return update -> {
            if (update.hasMyChatMember()) {
                ChatMemberUpdated myChatMember = update.getMyChatMember();
                return myChatMember.getNewChatMember().getStatus().equals("kicked");
            }
            return false;

        };
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
            String bookedDatesString = getDateAndPriceText(bookedDates, lang, pricePerDay);
            String text = carService.getCarName(book.getCarId()) + "\n" + bookedDatesString;
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(getChatId(upd))
                    .text(text)
                    .replyMarkup(keyboardMaker.getDeleteBookingKeyboard(book.getId(), lang))
                    .build();
            bot.getSilent().execute(sendMessage);
        }
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() &&
                update.getCallbackQuery().getData().contains(string);
    }

}
