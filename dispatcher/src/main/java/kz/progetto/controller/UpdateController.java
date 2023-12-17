package kz.progetto.controller;

import kz.progetto.service.UpdateProducer;
import kz.progetto.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import static kz.progetto.model.RabbitQueue.*;


@Component
@Log4j

public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;


    public UpdateController(MessageUtils messageUtils,  UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;


    }

    public void processUpdate(Update update){
        if (update == null){
            log.error("Received update is null");
            return;

        }
        if(update.getMessage() != null){
            disributeMessageByType(update);
        }else{
            log.error("Unsupported message type is recieved " + update);

        }
    }

    private void disributeMessageByType(Update update) {
        var message = update.getMessage();
        if(message.hasText()){
            processTextMessage(update);

        }else if (message.hasDocument()){
            processDocMessage(update);

        }else if(message.hasPhoto()){
            processPhotoMessage(update);
        }else{
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Неподдерживаемый тип сообщение!");
        setView(sendMessage);
    }

    private void setFileIsRecivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл Получен!Обрабатывается....");
        setView(sendMessage);

    }


    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE,update);
        setFileIsRecivedView(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE,update);
        setFileIsRecivedView(update);

    }



    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE,update);
        setFileIsRecivedView(update);


    }




}
