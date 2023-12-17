package kz.progetto.service.impl;

import kz.progetto.dao.AppUserDAO;
import kz.progetto.dao.RawDataDAO;
import kz.progetto.entity.AppDocument;
import kz.progetto.entity.AppUser;
import kz.progetto.entity.RawData;
import kz.progetto.exception.UploadFileException;
import kz.progetto.service.FileService;
import kz.progetto.service.MainService;
import kz.progetto.service.ProducerService;
import kz.progetto.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static kz.progetto.entity.enums.UserState.BASIC_STATE;
import static kz.progetto.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static kz.progetto.service.enums.ServiceCommands.*;

@Log4j
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, FileService fileService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;

        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text= update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommands.fromValue(text);

        if ( CANCEL.equals(text)){
            output = cancelProcess(appUser);
        }else if(BASIC_STATE.equals(userState)){
            output = processServiceCommand(appUser,text);
        }else if(WAIT_FOR_EMAIL_STATE.equals(userState)){
            //TODO:добавть обработку емейла
        }else{
            log.error("Unknown user state:" + userState);
            output = "Неизвестная ошибка! ВВедите /cancel и попробуйте снова!";
        }

        var chatId =  update.getMessage().getChatId();
        sendAnswer(output,chatId);


    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId =  update.getMessage().getChatId();
        if(isNotAllowToSendContent(chatId,appUser)){
            return;
        }
        //TODO добавить созранение документа
        var answer = "Фото успешно загружен! Ссылка для скачивание : tttp://test.kz/get-photo/777";
        sendAnswer(answer,chatId);

    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState =appUser.getState();
        if (!appUser.getIsActive()){
          var error = "Заригр=стрируйтесь или активизируйте свою учетную запись для загрузки контента";
        return true;
        }else if(!BASIC_STATE.equals(userState)){
            var error = "Отменит етекущую команду с помощью /cancel для отправки файлов";
            sendAnswer(error,chatId);
            return true;
        }
        return false;
    }


    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId =  update.getMessage().getChatId();
        if(isNotAllowToSendContent(chatId,appUser)){
            return;
        }
        //TODO добавить созранение фото
        try{
            AppDocument doc = fileService.processDoc(update.getMessage());
            var answer = "Документ успешно загружен!"
            +"Ссылка для скачивание : tttp://test.kz/get-doc/777";
            sendAnswer(answer,chatId);
        }catch(UploadFileException ex) {
            log.error(ex);
            String error = "К сожилению,загрузка файла не удалась.Повтарите попытку позже.";
            sendAnswer(error,chatId);

        }




    }

    private void sendAnswer(String output, Long chatId) {

        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);

    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //TODO добавить регистрацию
            return "Временно недоступно";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Приветвую! Чтобы посмотреть список доступных команд выведите /help";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд выведите /help";
        }
    }
        private String help() {
        return "Список доступных команд:\n"
                +"/cancel - отмена выпонения текущей команды;\n"
                +".registration - регистрация пользователя ";
        }


        private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update){
        var telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null){
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавление регистрации
                    .isActive(true)
                    .State(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}

