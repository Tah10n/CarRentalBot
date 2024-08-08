package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.tah10n.carrentalbot.utils.MessageHandlerUtil.editMessage;
import static com.github.tah10n.carrentalbot.utils.MessagesUtil.getMessage;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
public class StartAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;

    public StartAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService) {
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
                                , false, user.getLanguageCode());
                        myUserDAO.save(myUser);
                        String greeting = getMessage("greeting", myUser.getLanguage());
                        abilityBot.getSilent().send(greeting, ctx.chatId());
                    } else {
                        myUser = myUserDAO.getById(user.getId());
                    }
                    lang = myUser.getLanguage();


                    String text = getMessage("start", lang);
                    SendMessage message = SendMessage.builder()
                            .chatId(ctx.chatId().toString())
                            .text(text)
                            .replyMarkup(keyboardMaker.getStartKeyboard(lang,myUser.getId())).build();
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

        editMessage(bot, upd, chatId, messageId, text, backKeyboard);
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

    private void sendListOfBookings(BaseAbilityBot bot, Update upd) {
        MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
        String lang = myUser.getLanguage();
        List<BookingHistory> bookingsByUserId = carService.getBookingsByUserId(myUser.getId());
        for (BookingHistory book : bookingsByUserId) {
            String bookedDatesString = book.getBookedDates().stream().sorted()
                    .map(date -> date.format(DateTimeFormatter.ofPattern("dd.MM.yy")))
                    .collect(Collectors.joining(", "));
            String text = carService.getCarName(book.getCarId()) + "\n" + getMessage("your_bookings", lang) + bookedDatesString;
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
