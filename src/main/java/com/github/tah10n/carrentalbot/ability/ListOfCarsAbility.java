package com.github.tah10n.carrentalbot.ability;

import com.github.tah10n.carrentalbot.db.dao.MyUserDAO;
import com.github.tah10n.carrentalbot.db.entity.Car;
import com.github.tah10n.carrentalbot.db.entity.MyUser;
import com.github.tah10n.carrentalbot.keyboards.InlineKeyboardMaker;
import com.github.tah10n.carrentalbot.service.CarService;
import com.github.tah10n.carrentalbot.utils.MessagesUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
public class ListOfCarsAbility implements AbilityExtension {
    private final AbilityBot abilityBot;
    private final InlineKeyboardMaker keyboardMaker;
    private final MyUserDAO myUserDAO;
    private final CarService carService;
    private final MessagesUtil messagesUtil;

    public ListOfCarsAbility(AbilityBot abilityBot, InlineKeyboardMaker keyboardMaker, MyUserDAO myUserDAO, CarService carService, MessagesUtil messagesUtil) {
        this.abilityBot = abilityBot;
        this.keyboardMaker = keyboardMaker;
        this.myUserDAO = myUserDAO;
        this.carService = carService;
        this.messagesUtil = messagesUtil;
    }

    public ReplyFlow listOfCars() {
        return ReplyFlow.builder(abilityBot.getDb())
                .action(this::sendListOfCars)
                .onlyIf(hasCallbackQueryWith("list_of_cars"))
                .build();
    }

    private void sendListOfCars(BaseAbilityBot bot, Update upd) {
        List<Car> cars = carService.getAllCars();
        MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());

        if (Boolean.TRUE.equals(myUser.getIsAdmin())) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(getChatId(upd))
                    .text(messagesUtil.getMessage("count_of_cars", myUser.getLanguage()) + cars.size())
                    .replyMarkup(keyboardMaker.getAddCarKeyboard(myUser.getLanguage()))
                    .build();

            bot.getSilent().execute(sendMessage);
        }

        if (cars.isEmpty()) {
            String text = messagesUtil.getMessage("no_cars", upd.getCallbackQuery().getFrom().getLanguageCode());

            bot.getSilent().send(text, getChatId(upd));
            return;
        }


        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(getChatId(upd))
                .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                .build();

        bot.getSilent().execute(deleteMessage);

        for (Car car : cars) {
            if(car.getPhotoId() != null) {
                SendPhoto message = SendPhoto.builder()
                        .chatId(getChatId(upd))
                        .photo(new InputFile(car.getPhotoId()))
                        .caption(car.toString())
                        .replyMarkup(keyboardMaker.getChooseCarKeyboard(car.getId(), myUser.getLanguage()))
                        .build();
                try {
                    bot.getTelegramClient().execute(message);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            } else {
                SendMessage message = SendMessage.builder()
                        .chatId(getChatId(upd))
                        .text(car.toString())
                        .replyMarkup(keyboardMaker.getChooseCarKeyboard(car.getId(), myUser.getLanguage()))
                        .build();
                bot.getSilent().execute(message);
            }


        }
    }

    public ReplyFlow addCarButton() {

        Car car = new Car();
        ReplyFlow enterCarDescription = ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    Long chatId = upd.getMessage().getFrom().getId();
                    MyUser myUser = myUserDAO.getById(chatId);
                    String language = myUser.getLanguage();
                    car.setDescription(upd.getMessage().getText(), language);
                    car.setMap(new HashMap<>());

                    if (carService.addCar(car)) {
                        String text = messagesUtil.getMessage("car_added", language);
                        SendMessage message = SendMessage.builder()
                                .chatId(chatId)
                                .text(text)
                                .replyMarkup(keyboardMaker.getBackKeyboard(language))
                                .build();
                        bot.getSilent().execute(message);

                    } else {
                        bot.getSilent().send(messagesUtil.getMessage("car_not_added", language), chatId);
                    }
                })
                .onlyIf(isReplyToMessage("enter_car_description"))
                .build();

        ReplyFlow enterCarName = ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    MyUser myUser = myUserDAO.getById(upd.getMessage().getFrom().getId());
                    String language = myUser.getLanguage();
                    Long chatId = upd.getMessage().getFrom().getId();
                    car.setModel(upd.getMessage().getText());

                    bot.getSilent().forceReply(messagesUtil.getMessage("enter_car_description", language), chatId);
                })
                .onlyIf(isReplyToMessage("enter_car_name"))
                .next(enterCarDescription)
                .build();

        ReplyFlow enterCarPhoto = ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    MyUser myUser = myUserDAO.getById(upd.getMessage().getFrom().getId());
                    String language = myUser.getLanguage();
                    Long chatId = upd.getMessage().getFrom().getId();
                    car.setPhotoId(upd.getMessage().hasPhoto() ? upd.getMessage().getPhoto().stream()
                            .max(Comparator.comparing(PhotoSize::getFileSize))
                            .map(PhotoSize::getFileId)
                            .orElse("") : null);

                    bot.getSilent().forceReply(messagesUtil.getMessage("enter_car_name", language), chatId);
                })
                .onlyIf(isReplyToMessage("enter_car_photo"))
                .next(enterCarName)
                .build();


        return ReplyFlow.builder(abilityBot.getDb())
                .action((bot, upd) -> {
                    MyUser myUser = myUserDAO.getById(upd.getCallbackQuery().getFrom().getId());
                    String language = myUser.getLanguage();
                    Long chatId = upd.getCallbackQuery().getFrom().getId();
                    String text = messagesUtil.getMessage("enter_car_photo", language);

                    bot.getSilent().forceReply(text, chatId);

                })
                .onlyIf(hasCallbackQueryWith("add_car"))
                .next(enterCarPhoto)
                .build();
    }


    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            if (upd.getMessage() == null || upd.getMessage().getReplyToMessage() == null) return false;
            MyUser myUser = myUserDAO.getById(upd.getMessage().getFrom().getId());
            String lang = myUser.getLanguage();
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(messagesUtil.getMessage(message, lang));
        };
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
