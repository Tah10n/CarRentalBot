package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.CarDAO;
import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
public class ListOfCarsAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarDAO carDAO;
    private final MessagesUtil messagesUtil;

    public ListOfCarsAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarDAO carDAO, MessagesUtil messagesUtil) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carDAO = carDAO;
        this.messagesUtil = messagesUtil;
    }

    public ReplyFlow listOfCars() {
        return ReplyFlow                .builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    sendListOfCars(bot, upd);

                })
                .onlyIf(hasCallbackQueryWith("list_of_cars"))
                .build();
    }

    private void sendListOfCars(BaseAbilityBot bot, Update upd) {
        List<Car> cars = carDAO.getAll();
        MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());

        if(Boolean.TRUE.equals(myUser.getIsAdmin())) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(getChatId(upd))
                    .text(messagesUtil.getMessage("count_of_cars", myUser.getLanguage()) + cars.size())
                    .replyMarkup(keyboardMaker.getAddCarKeyboard(myUser.getLanguage()))
                    .build();

            bot.getSilent().execute(sendMessage);
        }

        if(cars.isEmpty()) {
            String text = messagesUtil.getMessage("no_cars", upd.getCallbackQuery().getFrom().getLanguageCode());

            bot.getSilent().send(text, getChatId(upd));
            return;
        }

        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(getChatId(upd))
                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                .build();

        bot.getSilent().execute(deleteMessage);

        for(Car car : cars) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(getChatId(upd))
                    .text(car.toString())
                    .parseMode("HTML")
                    .replyMarkup(keyboardMaker.getChooseCarKeyboard(car.getId(), myUser.getLanguage()))
                    .build();
            bot.getSilent().execute(sendMessage);
        }
    }

    public ReplyFlow addCarButton() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
                    String lang = myUser.getLanguage();
                    String chatId = upd.getCallbackQuery().getFrom().getId().toString();
                    String text = messagesUtil.getMessage("enter_car", lang);
                    SendMessage message = SendMessage.builder()
                            .chatId(chatId)
                            .text(text)
                            .replyMarkup(keyboardMaker.getBackKeyboard(lang))
                            .build();
                    bot.getSilent().execute(message);
                })
                .onlyIf(hasCallbackQueryWith("add_car"))
                .next(enterCar())
                .build();
    }

    public ReplyFlow enterCar() {

        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    String[] split = upd.getMessage().getText().split("::");
                    Car car = new Car();
                    String chatId = upd.getMessage().getFrom().getId().toString();
                    MyUser myUser = myUserDAO.getById(upd.getMessage().getFrom().getId());
                    String lang = myUser.getLanguage();
                    car.setModel(split[0]);
                    car.setDescription(split[1]);
                    car.setMap(new HashMap<>());
                    carDAO.save(car);
                    String text = messagesUtil.getMessage("car_added", lang);
                    SendMessage message = SendMessage.builder()
                            .chatId(chatId)
                            .text(text)
                            .replyMarkup(keyboardMaker.getBackKeyboard(lang))
                            .build();
                    bot.getSilent().execute(message);
                    System.out.println("1");
                })

                .onlyIf(hasMessageWithText("::"))
                .build();
    }

    private Predicate<Update> hasMessageWithText(String string) {
        return update -> update.hasMessage() &&
                         update.getMessage().getText().contains(string);
    }

    private Predicate<Update> hasCallbackQueryWith(String string) {
        return update -> update.hasCallbackQuery() &&
                         update.getCallbackQuery().getData().contains(string);
    }
}
